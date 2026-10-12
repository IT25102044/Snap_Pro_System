package com.event.snappro.SnapPro.dto;

import java.time.LocalDateTime;

public class EquipmentRequestActionRequest {

    private Integer requestId;

    // APPROVE or REJECT
    private String action;

    // Required when approving
    private Integer equipmentId;

    // Required when approving
    private LocalDateTime expectedReturnTime;

    private String coordinatorNotes;

    public EquipmentRequestActionRequest() {
    }

    public EquipmentRequestActionRequest(
            Integer requestId,
            String action,
            Integer equipmentId,
            LocalDateTime expectedReturnTime,
            String coordinatorNotes
    ) {
        this.requestId = requestId;
        this.action = action;
        this.equipmentId = equipmentId;
        this.expectedReturnTime = expectedReturnTime;
        this.coordinatorNotes = coordinatorNotes;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public LocalDateTime getExpectedReturnTime() {
        return expectedReturnTime;
    }

    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }

    public String getCoordinatorNotes() {
        return coordinatorNotes;
    }

    public void setCoordinatorNotes(String coordinatorNotes) {
        this.coordinatorNotes = coordinatorNotes;
    }
}