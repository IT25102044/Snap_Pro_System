package com.event.snappro.SnapPro.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_maintenance")
public class EquipmentMaintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "equipment_id",
            nullable = false
    )
    private EquipmentInventory equipment;

    @Column(
            name = "issue_description",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String issueDescription;

    @Column(
            name = "maintenance_type",
            nullable = false,
            length = 50
    )
    private String maintenanceType = "Repair";

    @Column(
            name = "cost",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(
            name = "logged_by",
            nullable = false
    )
    private Integer loggedBy;

    @Column(
            name = "status",
            nullable = false,
            length = 45
    )
    private String status = "Pending";

    @Column(
            name = "logged_at",
            nullable = false
    )
    private LocalDateTime loggedAt;

    @Column(name = "repaired_at")
    private LocalDateTime repairedAt;

    public EquipmentMaintenance() {
    }

    public EquipmentMaintenance(
            EquipmentInventory equipment,
            String issueDescription,
            String maintenanceType,
            BigDecimal cost,
            Integer loggedBy,
            String status
    ) {
        this.equipment = equipment;
        this.issueDescription = issueDescription;
        this.maintenanceType = maintenanceType;
        this.cost = cost;
        this.loggedBy = loggedBy;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }

        if (maintenanceType == null || maintenanceType.isBlank()) {
            maintenanceType = "Repair";
        }

        if (status == null || status.isBlank()) {
            status = "In Maintenance";
        }

        if (cost == null) {
            cost = BigDecimal.ZERO;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EquipmentInventory getEquipment() {
        return equipment;
    }

    public void setEquipment(EquipmentInventory equipment) {
        this.equipment = equipment;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Integer getLoggedBy() {
        return loggedBy;
    }

    public void setLoggedBy(Integer loggedBy) {
        this.loggedBy = loggedBy;
    }

    public Integer getReportedBy() {
        return loggedBy;
    }

    public void setReportedBy(Integer reportedBy) {
        this.loggedBy = reportedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }

    public LocalDateTime getRepairedAt() {
        return repairedAt;
    }

    public void setRepairedAt(LocalDateTime repairedAt) {
        this.repairedAt = repairedAt;
    }
}