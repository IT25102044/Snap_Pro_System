package com.event.snappro.SnapPro.dto;

public class EquipmentDamageRequest {

    private Integer equipmentId;

    private Integer allocationId;

    private String damageDescription;

    private String actionRoute;

    public EquipmentDamageRequest() {
    }

    public EquipmentDamageRequest(
            Integer equipmentId,
            Integer allocationId,
            String damageDescription,
            String actionRoute
    ) {
        this.equipmentId = equipmentId;
        this.allocationId = allocationId;
        this.damageDescription = damageDescription;
        this.actionRoute = actionRoute;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getId() {
        return allocationId;
    }

    public void setId(Integer id) {
        if (this.allocationId == null) {
            this.allocationId = id;
        }
    }

    public Integer getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Integer allocationId) {
        this.allocationId = allocationId;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }

    public String getDamageRemarks() {
        return damageDescription;
    }

    public void setDamageRemarks(String damageRemarks) {
        if (this.damageDescription == null || this.damageDescription.isBlank()) {
            this.damageDescription = damageRemarks;
        }
    }

    public String getActionRoute() {
        return actionRoute;
    }

    public void setActionRoute(String actionRoute) {
        this.actionRoute = actionRoute;
    }
}