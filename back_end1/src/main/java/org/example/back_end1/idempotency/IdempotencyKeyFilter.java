package org.example.back_end1.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class IdempotencyKeyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyKeyFilter.class);
    public static final String HEADER_NAME = "Idempotency-Key";
    public static final String USER_HEADER = "X-User-Id";

    private final IdempotencyKeyRepository repository;

    public IdempotencyKeyFilter(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/api/parts".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String idempotencyKey = request.getHeader(HEADER_NAME);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String userKey = resolveUserKey(request);
        String trimmedKey = idempotencyKey.trim();

        Optional<IdempotencyKeyRecord> existing = repository.findByUserKeyAndIdempotencyKey(userKey, trimmedKey);
        if (existing.isPresent()) {
            IdempotencyKeyRecord record = existing.get();
            log.info("[Lab10 Idempotency] Replaying stored response for user='{}', key='{}', status={}",
                    userKey, trimmedKey, record.getResponseStatus());
            response.setStatus(record.getResponseStatus());
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(record.getResponseBody());
            return;
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrappedResponse);

        int status = wrappedResponse.getStatus();
        byte[] bodyBytes = wrappedResponse.getContentAsByteArray();
        String bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);

        if (status < 500) {
            String hash = sha256(userKey + ":" + trimmedKey + ":" + bodyStr);
            repository.findByUserKeyAndIdempotencyKey(userKey, trimmedKey)
                    .orElseGet(() -> repository.save(new IdempotencyKeyRecord(
                            userKey,
                            trimmedKey,
                            hash,
                            status,
                            bodyStr,
                            Instant.now()
                    )));
            log.info("[Lab10 Idempotency] Stored response for user='{}', key='{}', status={}",
                    userKey, trimmedKey, status);
        } else {
            log.warn("[Lab10 Idempotency] Skipping storage for 5xx response (status={}) on user='{}', key='{}'",
                    status, userKey, trimmedKey);
        }

        wrappedResponse.copyBodyToResponse();
    }

    private String resolveUserKey(HttpServletRequest request) {
        String headerUser = request.getHeader(USER_HEADER);
        if (headerUser != null && !headerUser.isBlank()) {
            return headerUser.trim();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot compute SHA-256", e);
        }
    }
}