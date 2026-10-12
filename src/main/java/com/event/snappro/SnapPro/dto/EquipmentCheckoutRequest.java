package com.event.snappro.SnapPro.dto;

import java.time.LocalDateTime;

public class EquipmentCheckoutRequest {

    private Integer equipmentId;

    private Integer bookingId;

    private Integer photographerId;

    private LocalDateTime expectedReturnTime;

    public EquipmentCheckoutRequest() {
    }

    public EquipmentCheckoutRequest(
            Integer equipmentId,
            Integer bookingId,
            Integer photographerId,
            LocalDateTime expectedReturnTime
    ) {
        this.equipmentId = equipmentId;
        this.bookingId = bookingId;
        this.photographerId = photographerId;
        this.expectedReturnTime = expectedReturnTime;
    }

    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(Integer photographerId) {
        this.photographerId = photographerId;
    }

    public LocalDateTime getExpectedReturnTime() {
        return expectedReturnTime;
    }

    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }
}