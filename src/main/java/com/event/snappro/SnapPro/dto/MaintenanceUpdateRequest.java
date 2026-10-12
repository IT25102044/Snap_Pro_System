package com.event.snappro.SnapPro.dto;

import java.math.BigDecimal;

public class MaintenanceUpdateRequest {

    private Integer maintenanceId;

    private String status;

    private String maintenanceType;

    private BigDecimal cost;

    public MaintenanceUpdateRequest() {
    }

    public MaintenanceUpdateRequest(
            Integer maintenanceId,
            String status,
            String maintenanceType,
            BigDecimal cost
    ) {
        this.maintenanceId = maintenanceId;
        this.status = status;
        this.maintenanceType = maintenanceType;
        this.cost = cost;
    }

    public Integer getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(Integer maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}