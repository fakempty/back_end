package org.example.back_end1.auditing;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ActorRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        if (!(revisionEntity instanceof CustomRevisionEntity customRevision)) {
            return;
        }

        String batchActor = BatchImportActorContext.getActor();
        if (batchActor != null && !batchActor.isBlank()) {
            customRevision.setActor(batchActor);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            customRevision.setActor(SecurityContextAuditorAware.SYSTEM_USER);
            return;
        }

        String name = auth.getName();
        customRevision.setActor((name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name))
                ? SecurityContextAuditorAware.SYSTEM_USER
                : name);
    }
}