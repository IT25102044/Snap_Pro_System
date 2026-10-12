package com.event.snappro.SnapPro.controller;

import com.event.snappro.SnapPro.dto.EquipmentDashboardResponse;
import com.event.snappro.SnapPro.service.EquipmentDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment/dashboard")
public class EquipmentDashboardController {

    private final EquipmentDashboardService equipmentDashboardService;

    public EquipmentDashboardController(
            EquipmentDashboardService equipmentDashboardService
    ) {
        this.equipmentDashboardService = equipmentDashboardService;
    }

    // =========================================================
    // GET DASHBOARD SUMMARY
    // =========================================================

    @GetMapping
    public ResponseEntity<EquipmentDashboardResponse>
    getDashboardSummary() {

        EquipmentDashboardResponse response =
                equipmentDashboardService.getDashboardSummary();

        return ResponseEntity.ok(response);
    }
}