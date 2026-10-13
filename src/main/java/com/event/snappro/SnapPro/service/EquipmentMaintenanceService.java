package com.event.snappro.SnapPro.service;

import com.event.snappro.SnapPro.dto.EquipmentMaintenanceResponse;
import com.event.snappro.SnapPro.dto.MaintenanceUpdateRequest;

import java.util.List;

public interface EquipmentMaintenanceService {

    // Get all maintenance records
    List<EquipmentMaintenanceResponse> getAllMaintenanceRecords();

    // Get only active maintenance records
    List<EquipmentMaintenanceResponse> getActiveMaintenanceRecords();

    // Get completed/repaired maintenance records
    List<EquipmentMaintenanceResponse> getCompletedMaintenanceRecords();

    // Get one maintenance record
    EquipmentMaintenanceResponse getMaintenanceById(
            Integer maintenanceId
    );

    // Get maintenance history for one equipment item
    List<EquipmentMaintenanceResponse> getMaintenanceByEquipment(
            Integer equipmentId
    );

    // Update maintenance status/type/cost
    EquipmentMaintenanceResponse updateMaintenance(
            MaintenanceUpdateRequest request
    );

    // Mark equipment as repaired and return it to Available status
    EquipmentMaintenanceResponse markAsRepaired(
            Integer maintenanceId
    );
}
