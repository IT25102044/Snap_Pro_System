package com.event.snappro.SnapPro.dto;

public class EquipmentReturnRequest {

    private Integer allocationId;

    private String returnNotes;

    private boolean reportedDamage;

    private String damageRemarks;

    public EquipmentReturnRequest() {
    }

    public EquipmentReturnRequest(
            Integer allocationId,
            String returnNotes,
            boolean reportedDamage,
            String damageRemarks
    ) {
        this.allocationId = allocationId;
        this.returnNotes = returnNotes;
        this.reportedDamage = reportedDamage;
        this.damageRemarks = damageRemarks;
    }

    public Integer getId() {
        return allocationId;
    }

    public void setId(Integer id) {
        this.allocationId = id;
    }

    public Integer getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Integer allocationId) {
        this.allocationId = allocationId;
    }

    public String getReturnNotes() {
        return returnNotes;
    }

    public void setReturnNotes(String returnNotes) {
        this.returnNotes = returnNotes;
    }

    public boolean isReportedDamage() {
        return reportedDamage;
    }

    public void setReportedDamage(boolean reportedDamage) {
        this.reportedDamage = reportedDamage;
    }

    public String getDamageRemarks() {
        return damageRemarks;
    }

    public void setDamageRemarks(String damageRemarks) {
        this.damageRemarks = damageRemarks;
    }

    public String getDamageDescription() {
        return damageRemarks;
    }

    public void setDamageDescription(String damageDescription) {
        if (this.damageRemarks == null || this.damageRemarks.isBlank()) {
            this.damageRemarks = damageDescription;
        }
    }
}