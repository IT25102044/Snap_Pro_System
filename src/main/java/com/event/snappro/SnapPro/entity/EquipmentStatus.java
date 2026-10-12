package com.event.snappro.SnapPro.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment_status")
public class EquipmentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "status", nullable = false, length = 45)
    private String status;

    // Default constructor required by JPA
    public EquipmentStatus() {
    }

    public EquipmentStatus(String status) {
        this.status = status;
    }

    public EquipmentStatus(Integer id, String status) {
        this.id = id;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}