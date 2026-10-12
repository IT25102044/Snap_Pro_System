package com.event.snappro.SnapPro.repository;

import com.event.snappro.SnapPro.entity.EquipmentInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentInventoryRepository
        extends JpaRepository<EquipmentInventory, Integer> {

    // Find equipment using barcode / serial number
    Optional<EquipmentInventory> findBySerialNumber(String serialNumber);

    // Check whether a serial number already exists
    boolean existsBySerialNumber(String serialNumber);

    // Get only equipment that has not been decommissioned
    List<EquipmentInventory> findByDecommissionedFalse();

    // Get equipment by status
    List<EquipmentInventory> findByEquipmentStatusIdAndDecommissionedFalse(
            Integer equipmentStatusId
    );

    // Count equipment by status for dashboard cards
    long countByEquipmentStatusIdAndDecommissionedFalse(
            Integer equipmentStatusId
    );

    // Search by item name
    List<EquipmentInventory>
    findByItemNameContainingIgnoreCaseAndDecommissionedFalse(
            String itemName
    );

    // Search by category
    List<EquipmentInventory>
    findByCategoryIgnoreCaseAndDecommissionedFalse(
            String category
    );
}