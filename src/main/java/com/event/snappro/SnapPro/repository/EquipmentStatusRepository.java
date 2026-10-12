package com.event.snappro.SnapPro.repository;

import com.event.snappro.SnapPro.entity.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentStatusRepository
        extends JpaRepository<EquipmentStatus, Integer> {

    Optional<EquipmentStatus> findByStatus(String status);
}