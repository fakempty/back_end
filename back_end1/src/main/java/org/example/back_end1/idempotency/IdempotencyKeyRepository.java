package org.example.back_end1.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyRecord, Long> {
    Optional<IdempotencyKeyRecord> findByUserKeyAndIdempotencyKey(String userKey, String idempotencyKey);
}