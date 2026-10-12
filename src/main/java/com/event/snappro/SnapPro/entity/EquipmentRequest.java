package com.event.snappro.SnapPro.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_requests")
public class EquipmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // References event_bookings.id
    // Kept as Integer because event_bookings belongs to another module.
    @Column(
            name = "booking_id",
            nullable = false
    )
    private Integer bookingId;

    // References photographer_profiles.id
    // Kept as Integer because photographer_profiles belongs to another module.
    @Column(
            name = "photographer_id",
            nullable = false
    )
    private Integer photographerId;

    @Column(
            name = "requested_item_type",
            nullable = false,
            length = 150
    )
    private String requestedItemType;

    @Column(
            name = "urgency_level",
            nullable = false,
            length = 45
    )
    private String urgencyLevel = "Normal";

    @Column(
            name = "request_notes",
            columnDefinition = "TEXT"
    )
    private String requestNotes;

    @Column(
            name = "request_status",
            nullable = false,
            length = 45
    )
    private String requestStatus = "Pending";

    @Column(
            name = "requested_at",
            nullable = false
    )
    private LocalDateTime requestedAt;

    // References users.id
    // Equipment Coordinator who handled the request
    @Column(name = "handled_by")
    private Integer handledBy;

    @Column(
            name = "coordinator_notes",
            columnDefinition = "TEXT"
    )
    private String coordinatorNotes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Links an approved/dispatched request to the created allocation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfilled_allocation_id")
    private EquipmentAllocation fulfilledAllocation;

    public EquipmentRequest() {
    }

    public EquipmentRequest(
            Integer bookingId,
            Integer photographerId,
            String requestedItemType,
            String urgencyLevel,
            String requestNotes
    ) {
        this.bookingId = bookingId;
        this.photographerId = photographerId;
        this.requestedItemType = requestedItemType;
        this.urgencyLevel = urgencyLevel;
        this.requestNotes = requestNotes;
    }

    @PrePersist
    protected void onCreate() {

        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }

        if (urgencyLevel == null || urgencyLevel.isBlank()) {
            urgencyLevel = "Normal";
        }

        if (requestStatus == null || requestStatus.isBlank()) {
            requestStatus = "Pending";
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public EquipmentAllocation getFulfilledAllocation() {
        return fulfilledAllocation;
    }

    public void setFulfilledAllocation(
            EquipmentAllocation fulfilledAllocation
    ) {
        this.fulfilledAllocation = fulfilledAllocation;
    }
}