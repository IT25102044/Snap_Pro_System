package com.event.snappro.SnapPro.dto;

import java.time.LocalDateTime;

public class EquipmentRequestResponse {

    private Integer requestId;

    private Integer bookingId;

    private String bookingReference;

    private Integer photographerId;

    private String photographerName;

    private String requestedItemType;

    private String urgencyLevel;

    private String requestNotes;

    private String requestStatus;

    private LocalDateTime requestedAt;

    private Integer handledBy;

    private String handledByName;

    private String coordinatorNotes;

    private LocalDateTime resolvedAt;

    private Integer fulfilledAllocationId;

    public EquipmentRequestResponse() {
    }

    public EquipmentRequestResponse(
            Integer requestId,
            Integer bookingId,
            String bookingReference,
            Integer photographerId,
            String photographerName,
            String requestedItemType,
            String urgencyLevel,
            String requestNotes,
            String requestStatus,
            LocalDateTime requestedAt,
            Integer handledBy,
            String handledByName,
            String coordinatorNotes,
            LocalDateTime resolvedAt,
            Integer fulfilledAllocationId
    ) {
        this.requestId = requestId;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.photographerId = photographerId;
        this.photographerName = photographerName;
        this.requestedItemType = requestedItemType;
        this.urgencyLevel = urgencyLevel;
        this.requestNotes = requestNotes;
        this.requestStatus = requestStatus;
        this.requestedAt = requestedAt;
        this.handledBy = handledBy;
        this.handledByName = handledByName;
        this.coordinatorNotes = coordinatorNotes;
        this.resolvedAt = resolvedAt;
        this.fulfilledAllocationId = fulfilledAllocationId;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
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

    public String getRequestedItemType() {
        return requestedItemType;
    }

    public void setRequestedItemType(String requestedItemType) {
        this.requestedItemType = requestedItemType;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getRequestNotes() {
        return requestNotes;
    }

    public void setRequestNotes(String requestNotes) {
        this.requestNotes = requestNotes;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
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

    public String getCoordinatorNotes() {
        return coordinatorNotes;
    }

    public void setCoordinatorNotes(String coordinatorNotes) {
        this.coordinatorNotes = coordinatorNotes;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Integer getFulfilledAllocationId() {
        return fulfilledAllocationId;
    }

    public void setFulfilledAllocationId(Integer fulfilledAllocationId) {
        this.fulfilledAllocationId = fulfilledAllocationId;
    }
}