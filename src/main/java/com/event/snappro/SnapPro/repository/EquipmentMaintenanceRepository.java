package com.event.snappro.SnapPro.repository;

import com.event.snappro.SnapPro.entity.EquipmentMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentMaintenanceRepository
        extends JpaRepository<EquipmentMaintenance, Integer> {

    // Get all maintenance records for one equipment item
    List<EquipmentMaintenance>
    findByEquipmentIdOrderByLoggedAtDesc(
            Integer equipmentId
    );

    // Get maintenance records by status
    List<EquipmentMaintenance>
    findByStatusIgnoreCaseOrderByLoggedAtDesc(
            String status
    );

    // Get active maintenance records
    List<EquipmentMaintenance>
    findByRepairedAtIsNullOrderByLoggedAtDesc();

    // Get completed maintenance records
    List<EquipmentMaintenance>
    findByRepairedAtIsNotNullOrderByRepairedAtDesc();

    // Find the latest active maintenance record for equipment
    Optional<EquipmentMaintenance>
    findFirstByEquipmentIdAndRepairedAtIsNullOrderByLoggedAtDesc(
            Integer equipmentId
    );

    // Check whether equipment already has an active repair record
    boolean existsByEquipmentIdAndRepairedAtIsNull(
            Integer equipmentId
    );

    // Dashboard / admin count
    long countByRepairedAtIsNull();
}