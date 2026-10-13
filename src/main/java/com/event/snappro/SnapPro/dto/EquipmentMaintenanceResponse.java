package com.event.snappro.SnapPro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EquipmentMaintenanceResponse {

    private Integer maintenanceId;

    private String rmaReference;

    private Integer equipmentId;

    private String serialNumber;

    private String itemName;

    private String model;

    private String issueDescription;

    private String maintenanceType;

    private BigDecimal cost;

    private Integer loggedBy;

    private String loggedByName;

    private String status;

    private LocalDateTime loggedAt;

    private LocalDateTime repairedAt;

    public EquipmentMaintenanceResponse() {
    }

    public EquipmentMaintenanceResponse(
            Integer maintenanceId,
            String rmaReference,
            Integer equipmentId,
            String serialNumber,
            String itemName,
            String model,
            String issueDescription,
            String maintenanceType,
            BigDecimal cost,
            Integer loggedBy,
            String loggedByName,
            String status,
            LocalDateTime loggedAt,
            LocalDateTime repairedAt
    ) {
        this.maintenanceId = maintenanceId;
        this.rmaReference = rmaReference;
        this.equipmentId = equipmentId;
        this.serialNumber = serialNumber;
        this.itemName = itemName;
        this.model = model;
        this.issueDescription = issueDescription;
        this.maintenanceType = maintenanceType;
        this.cost = cost;
        this.loggedBy = loggedBy;
        this.loggedByName = loggedByName;
        this.status = status;
        this.loggedAt = loggedAt;
        this.repairedAt = repairedAt;
    }

    public Integer getId() {
        return maintenanceId;
    }

    public void setId(Integer id) {
        this.maintenanceId = id;
    }

    public Integer getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(Integer maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public String getRmaReference() {
        return rmaReference;
    }

    public void setRmaReference(String rmaReference) {
        this.rmaReference = rmaReference;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
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

    public String getLoggedByName() {
        return loggedByName;
    }

    public void setLoggedByName(String loggedByName) {
        this.loggedByName = loggedByName;
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