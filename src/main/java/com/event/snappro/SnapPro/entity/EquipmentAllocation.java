package com.event.snappro.SnapPro.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_allocations")
public class EquipmentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "equipment_id",
            nullable = false
    )
    private EquipmentInventory equipment;

    @Column(
            name = "booking_id",
            nullable = false
    )
    private Integer bookingId;

    @Column(
            name = "photographer_id",
            nullable = false
    )
    private Integer photographerId;

    @Column(
            name = "handled_by",
            nullable = false
    )
    private Integer handledBy;

    @Column(
            name = "checkout_time",
            nullable = false
    )
    private LocalDateTime checkoutTime;

    @Column(name = "expected_return_time")
    private LocalDateTime expectedReturnTime;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(
            name = "return_notes",
            columnDefinition = "TEXT"
    )
    private String returnNotes;

    @Column(
            name = "reported_damage",
            nullable = false
    )
    private boolean reportedDamage = false;

    @Column(
            name = "damage_remarks",
            columnDefinition = "TEXT"
    )
    private String damageRemarks;

    public EquipmentAllocation() {
    }

    public EquipmentAllocation(
            EquipmentInventory equipment,
            Integer bookingId,
            Integer photographerId,
            Integer handledBy,
            LocalDateTime checkoutTime,
            LocalDateTime expectedReturnTime
    ) {
        this.equipment = equipment;
        this.bookingId = bookingId;
        this.photographerId = photographerId;
        this.handledBy = handledBy;
        this.checkoutTime = checkoutTime;
        this.expectedReturnTime = expectedReturnTime;
    }

    @PrePersist
    protected void onCreate() {
        if (checkoutTime == null) {
            checkoutTime = LocalDateTime.now();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EquipmentInventory getEquipment() {
        return equipment;
    }

    public void setEquipment(EquipmentInventory equipment) {
        this.equipment = equipment;
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

    public Integer getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(Integer handledBy) {
        this.handledBy = handledBy;
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
}