package org.example.back_end1.auditing;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PartAuditHistoryService {

    private final EntityManagerFactory entityManagerFactory;

    public PartAuditHistoryService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public List<PartHistoryDto> getPartHistory(Long partId) {
        var em = entityManagerFactory.createEntityManager();
        try {
            AuditReader auditReader = AuditReaderFactory.get(em);
            AuditQuery query = auditReader.createQuery()
                    .forRevisionsOfEntity(org.example.back_end1.entity.Part.class, false, true)
                    .add(AuditEntity.id().eq(partId))
                    .addOrder(AuditEntity.revisionNumber().asc());

            @SuppressWarnings("unchecked")
            List<Object[]> rawResults = query.getResultList();
            List<PartHistoryDto> result = new ArrayList<>(rawResults.size());

            for (Object[] row : rawResults) {
                org.example.back_end1.entity.Part partSnapshot = (org.example.back_end1.entity.Part) row[0];
                CustomRevisionEntity revEntity = (CustomRevisionEntity) row[1];
                RevisionType revType = (RevisionType) row[2];

                result.add(new PartHistoryDto(
                        revEntity.getId(),
                        revType,
                        Instant.ofEpochMilli(revEntity.getTimestamp()),
                        revEntity.getActor(),
                        partId,
                        partSnapshot != null ? partSnapshot.getName() : null,
                        partSnapshot != null ? partSnapshot.getSku() : null,
                        partSnapshot != null ? partSnapshot.getPrice() : null,
                        partSnapshot != null ? partSnapshot.getQuantity() : null,
                        partSnapshot != null ? partSnapshot.getCreatedBy() : null,
                        partSnapshot != null ? partSnapshot.getUpdatedBy() : null
                ));
            }
            return result;
        } finally {
            em.close();
        }
    }
}