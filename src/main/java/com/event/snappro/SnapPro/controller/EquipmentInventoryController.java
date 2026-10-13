package com.event.snappro.SnapPro.controller;

import com.event.snappro.SnapPro.dto.EquipmentInventoryRequest;
import com.event.snappro.SnapPro.dto.EquipmentInventoryResponse;
import com.event.snappro.SnapPro.service.EquipmentInventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment/inventory")
public class EquipmentInventoryController {

    private final EquipmentInventoryService equipmentInventoryService;

    public EquipmentInventoryController(
            EquipmentInventoryService equipmentInventoryService
    ) {
        this.equipmentInventoryService = equipmentInventoryService;
    }

    // =========================================================
    // GET ALL ACTIVE EQUIPMENT
    // =========================================================

    @GetMapping
    public ResponseEntity<List<EquipmentInventoryResponse>>
    getAllEquipment() {

        return ResponseEntity.ok(
                equipmentInventoryService.getAllEquipment()
        );
    }


    // =========================================================
    // GET AVAILABLE EQUIPMENT
    // =========================================================

    @GetMapping("/available")
    public ResponseEntity<List<EquipmentInventoryResponse>>
    getAvailableEquipment() {

        return ResponseEntity.ok(
                equipmentInventoryService.getAvailableEquipment()
        );
    }


    // =========================================================
    // GET EQUIPMENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentInventoryResponse>
    getEquipmentById(
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                equipmentInventoryService.getEquipmentById(id)
        );
    }


    // =========================================================
    // GET EQUIPMENT BY SERIAL NUMBER / BARCODE
    // =========================================================

    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<EquipmentInventoryResponse>
    getEquipmentBySerialNumber(
            @PathVariable String serialNumber
    ) {

        return ResponseEntity.ok(
                equipmentInventoryService
                        .getEquipmentBySerialNumber(serialNumber)
        );
    }


    // =========================================================
    // SEARCH EQUIPMENT BY ITEM NAME
    // =========================================================

    @GetMapping("/search")
    public ResponseEntity<List<EquipmentInventoryResponse>>
    searchEquipment(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                equipmentInventoryService.searchEquipment(name)
        );
    }


    // =========================================================
    // FILTER EQUIPMENT BY CATEGORY
    // =========================================================

    @GetMapping("/category")
    public ResponseEntity<List<EquipmentInventoryResponse>>
    getEquipmentByCategory(
            @RequestParam String category
    ) {

        return ResponseEntity.ok(
                equipmentInventoryService
                        .getEquipmentByCategory(category)
        );
    }
    
    // =========================================================
    // CREATE EQUIPMENT
    // =========================================================

    @PostMapping
    public ResponseEntity<EquipmentInventoryResponse>
    createEquipment(
            @RequestBody EquipmentInventoryRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipmentInventoryService.createEquipment(request));
    }


    // =========================================================
    // UPDATE EQUIPMENT
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentInventoryResponse>
    updateEquipment(
            @PathVariable Integer id,
            @RequestBody EquipmentInventoryRequest request
    ) {

        return ResponseEntity.ok(
                equipmentInventoryService.updateEquipment(id, request)
        );
    }


    // =========================================================
    // DELETE EQUIPMENT
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteEquipment(
            @PathVariable Integer id
    ) {

        equipmentInventoryService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}