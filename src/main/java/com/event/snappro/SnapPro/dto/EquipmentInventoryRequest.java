package com.event.snappro.SnapPro.dto;

public class EquipmentInventoryRequest {

    private String serialNumber;
    private String itemName;
    private String model;
    private String category;
    private Integer statusId;
    private String notes;

    public EquipmentInventoryRequest() {
    }

    public EquipmentInventoryRequest(String serialNumber, String itemName, String model, String category, Integer statusId, String notes) {
        this.serialNumber = serialNumber;
        this.itemName = itemName;
        this.model = model;
        this.category = category;
        this.statusId = statusId;
        this.notes = notes;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
