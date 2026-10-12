package com.event.snappro.SnapPro.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_inventory")
public class EquipmentInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "serial_number",
            nullable = false,
            unique = true,
            length = 100
    )
    private String serialNumber;

    @Column(
            name = "item_name",
            nullable = false,
            length = 150
    )
    private String itemName;

    @Column(
            name = "model",
            length = 100
    )
    private String model;

    @Column(
            name = "category",
            nullable = false,
            length = 100
    )
    private String category = "Camera Body";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "equipment_status_id",
            nullable = false
    )
    private EquipmentStatus equipmentStatus;

    @Column(
            name = "notes",
            columnDefinition = "TEXT"
    )
    private String notes;

    @Column(
            name = "is_decommissioned",
            nullable = false
    )
    private boolean decommissioned = false;

    // JPA requires a no-argument constructor
    public EquipmentInventory() {
    }

    @PrePersist
    @PreUpdate
    protected void ensureCategory() {
        if (category == null || category.isBlank() || "-".equals(category.trim())) {
            category = "General";
        }
    }

    public EquipmentInventory(
            String serialNumber,
            String itemName,
            String model,
            String category,
            EquipmentStatus equipmentStatus,
            String notes
    ) {
        this.serialNumber = serialNumber;
        this.itemName = itemName;
        this.model = model;
        this.category = category;
        this.equipmentStatus = equipmentStatus;
        this.notes = notes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public EquipmentStatus getEquipmentStatus() {
        return equipmentStatus;
    }

    public void setEquipmentStatus(EquipmentStatus equipmentStatus) {
        this.equipmentStatus = equipmentStatus;
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