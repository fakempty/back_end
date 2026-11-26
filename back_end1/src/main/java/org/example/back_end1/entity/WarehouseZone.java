package org.example.back_end1.entity;

import jakarta.persistence.*;
import org.example.back_end1.auditing.AuditableEntity;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "warehouse_zones")
@Audited
public class WarehouseZone extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_code", nullable = false, unique = true)
    private String zoneCode;

    private String description;

    @Column(nullable = false)
    private Integer capacity = 100;

    public WarehouseZone() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getZoneCode() { return zoneCode; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}