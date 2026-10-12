package com.event.snappro.SnapPro.dto;

import java.time.LocalDateTime;

public class EquipmentAllocationResponse {

    private Integer allocationId;

    private Integer equipmentId;

    private String serialNumber;

    private String itemName;

    private String model;

    private Integer bookingId;

    private String bookingReference;

    private String eventName;

    private Integer photographerId;

    private String photographerName;

    private Integer handledBy;

    private String handledByName;

    private LocalDateTime checkoutTime;

    private LocalDateTime expectedReturnTime;

    private LocalDateTime returnTime;

    private String returnNotes;

    private boolean reportedDamage;

    private String damageRemarks;

    private String equipmentStatus;

    public EquipmentAllocationResponse() {
    }

    public EquipmentAllocationResponse(
            Integer allocationId,
            Integer equipmentId,
            String serialNumber,
            String itemName,
            String model,
            Integer bookingId,
            String bookingReference,
            Integer photographerId,
            String photographerName,
            Integer handledBy,
            String handledByName,
            LocalDateTime checkoutTime,
            LocalDateTime expectedReturnTime,
            LocalDateTime returnTime,
            String returnNotes,
            boolean reportedDamage,
            String damageRemarks,
            String equipmentStatus
    ) {
        this.allocationId = allocationId;
        this.equipmentId = equipmentId;
        this.serialNumber = serialNumber;
        this.itemName = itemName;
        this.model = model;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.photographerId = photographerId;
        this.photographerName = photographerName;
        this.handledBy = handledBy;
        this.handledByName = handledByName;
        this.checkoutTime = checkoutTime;
        this.expectedReturnTime = expectedReturnTime;
        this.returnTime = returnTime;
        this.returnNotes = returnNotes;
        this.reportedDamage = reportedDamage;
        this.damageRemarks = damageRemarks;
        this.equipmentStatus = equipmentStatus;
    }

    public EquipmentAllocationResponse(
            Integer allocationId,
            Integer equipmentId,
            String serialNumber,
            String itemName,
            String model,
            Integer bookingId,
            String bookingReference,
            String eventName,
            Integer photographerId,
            String photographerName,
            Integer handledBy,
            String handledByName,
            LocalDateTime checkoutTime,
            LocalDateTime expectedReturnTime,
            LocalDateTime returnTime,
            String returnNotes,
            boolean reportedDamage,
            String damageRemarks,
            String equipmentStatus
    ) {
        this.allocationId = allocationId;
        this.equipmentId = equipmentId;
        this.serialNumber = serialNumber;
        this.itemName = itemName;
        this.model = model;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.eventName = eventName;
        this.photographerId = photographerId;
        this.photographerName = photographerName;
        this.handledBy = handledBy;
        this.handledByName = handledByName;
        this.checkoutTime = checkoutTime;
        this.expectedReturnTime = expectedReturnTime;
        this.returnTime = returnTime;
        this.returnNotes = returnNotes;
        this.reportedDamage = reportedDamage;
        this.damageRemarks = damageRemarks;
        this.equipmentStatus = equipmentStatus;
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

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getEventId() {
        return bookingId;
    }

    public void setEventId(Integer eventId) {
        this.bookingId = eventId;
    }

    public Integer getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(Integer photographerId) {
        this.photographerId = photographerId;
    }

    public String getPhotographerName() {
        return photographerName;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    public Integer getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(Integer handledBy) {
        this.handledBy = handledBy;
    }

    public String getHandledByName() {
        return handledByName;
    }

    public void setHandledByName(String handledByName) {
        this.handledByName = handledByName;
    }

    public LocalDateTime getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(LocalDateTime checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public LocalDateTime getExpectedReturnTime() {
        return expectedReturnTime;
    }

    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }

    public LocalDateTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(LocalDateTime returnTime) {
        this.returnTime = returnTime;
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

    public String getEquipmentStatus() {
        return equipmentStatus;
    }

    public void setEquipmentStatus(String equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
    }

    public String getStatus() {
        return equipmentStatus;
    }

    public void setStatus(String status) {
        this.equipmentStatus = status;
    }
}