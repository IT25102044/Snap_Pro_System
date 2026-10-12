package com.event.snappro.SnapPro.controller;

import com.event.snappro.SnapPro.dto.EquipmentMaintenanceResponse;
import com.event.snappro.SnapPro.dto.MaintenanceUpdateRequest;
import com.event.snappro.SnapPro.service.EquipmentMaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment/maintenance")
public class EquipmentMaintenanceController {

    private final EquipmentMaintenanceService equipmentMaintenanceService;

    public EquipmentMaintenanceController(
            EquipmentMaintenanceService equipmentMaintenanceService
    ) {
        this.equipmentMaintenanceService = equipmentMaintenanceService;
    }


    // =========================================================
    // GET ALL MAINTENANCE RECORDS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<EquipmentMaintenanceResponse>>
    getAllMaintenanceRecords() {

        return ResponseEntity.ok(
                equipmentMaintenanceService.getAllMaintenanceRecords()
        );
    }


    // =========================================================
    // GET ACTIVE MAINTENANCE RECORDS
    // =========================================================

    @GetMapping("/active")
    public ResponseEntity<List<EquipmentMaintenanceResponse>>
    getActiveMaintenanceRecords() {

        return ResponseEntity.ok(
                equipmentMaintenanceService.getActiveMaintenanceRecords()
        );
    }


    // =========================================================
    // GET COMPLETED MAINTENANCE RECORDS
    // =========================================================

    @GetMapping("/completed")
    public ResponseEntity<List<EquipmentMaintenanceResponse>>
    getCompletedMaintenanceRecords() {

        return ResponseEntity.ok(
                equipmentMaintenanceService.getCompletedMaintenanceRecords()
        );
    }


    // =========================================================
    // GET ONE MAINTENANCE RECORD
    // =========================================================

    @GetMapping("/{maintenanceId}")
    public ResponseEntity<EquipmentMaintenanceResponse>
    getMaintenanceById(
            @PathVariable Integer maintenanceId
    ) {

        return ResponseEntity.ok(
                equipmentMaintenanceService
                        .getMaintenanceById(maintenanceId)
        );
    }


    // =========================================================
    // GET MAINTENANCE HISTORY FOR ONE EQUIPMENT ITEM
    // =========================================================

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<EquipmentMaintenanceResponse>>
    getMaintenanceByEquipment(
            @PathVariable Integer equipmentId
    ) {

        return ResponseEntity.ok(
                equipmentMaintenanceService
                        .getMaintenanceByEquipment(equipmentId)
        );
    }


    // =========================================================
    // UPDATE MAINTENANCE RECORD
    // =========================================================

    @PutMapping("/update")
    public ResponseEntity<EquipmentMaintenanceResponse>
    updateMaintenance(
            @RequestBody MaintenanceUpdateRequest request
    ) {

        EquipmentMaintenanceResponse response =
                equipmentMaintenanceService
                        .updateMaintenance(request);

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // MARK EQUIPMENT AS REPAIRED
    // =========================================================

    @PutMapping("/{maintenanceId}/repair")
    public ResponseEntity<EquipmentMaintenanceResponse>
    markAsRepaired(
            @PathVariable Integer maintenanceId
    ) {

        EquipmentMaintenanceResponse response =
                equipmentMaintenanceService
                        .markAsRepaired(maintenanceId);

        return ResponseEntity.ok(response);
    }
}