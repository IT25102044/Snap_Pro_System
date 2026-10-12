package com.event.snappro.SnapPro.dto;

public class EquipmentInventoryResponse {

    private Integer id;

    private String serialNumber;

    private String itemName;

    private String model;

    private String category;

    private Integer statusId;

    private String status;

    private String notes;

    private boolean decommissioned;

    public EquipmentInventoryResponse() {
    }

    public EquipmentInventoryResponse(
            Integer id,
            String serialNumber,
            String itemName,
            String model,
            String category,
            Integer statusId,
            String status,
            String notes,
            boolean decommissioned
    ) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.itemName = itemName;
        this.model = model;
        this.category = category;
        this.statusId = statusId;
        this.status = status;
        this.notes = notes;
        this.decommissioned = decommissioned;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEquipmentId() {
        return id;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.id = equipmentId;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isDecommissioned() {
        return decommissioned;
    }

    public void setDecommissioned(boolean decommissioned) {
        this.decommissioned = decommissioned;
    }
}