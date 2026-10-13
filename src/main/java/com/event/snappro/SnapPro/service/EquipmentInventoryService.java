package com.event.snappro.SnapPro.service;

import com.event.snappro.SnapPro.dto.EquipmentInventoryRequest;
import com.event.snappro.SnapPro.dto.EquipmentInventoryResponse;

import java.util.List;

public interface EquipmentInventoryService {

    // Get all active / non-decommissioned equipment
    List<EquipmentInventoryResponse> getAllEquipment();

    // Get equipment that is currently available for checkout
    List<EquipmentInventoryResponse> getAvailableEquipment();

    // Get one equipment item using database ID
    EquipmentInventoryResponse getEquipmentById(Integer id);

    // Find equipment using barcode / serial number
    EquipmentInventoryResponse getEquipmentBySerialNumber(
            String serialNumber
    );

    // Search equipment by item name
    List<EquipmentInventoryResponse> searchEquipment(
            String itemName
    );

    // Filter equipment by category
    List<EquipmentInventoryResponse> getEquipmentByCategory(
            String category
    );

    // Create new equipment
    EquipmentInventoryResponse createEquipment(EquipmentInventoryRequest request);

    // Update existing equipment
    EquipmentInventoryResponse updateEquipment(Integer id, EquipmentInventoryRequest request);

    // Delete equipment (soft decommission)
    void deleteEquipment(Integer id);
}