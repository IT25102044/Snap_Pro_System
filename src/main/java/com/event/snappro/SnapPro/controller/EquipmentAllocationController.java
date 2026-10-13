package com.event.snappro.SnapPro.controller;

import com.event.snappro.SnapPro.dto.EquipmentAllocationResponse;
import com.event.snappro.SnapPro.dto.EquipmentCheckoutRequest;
import com.event.snappro.SnapPro.dto.EquipmentDamageRequest;
import com.event.snappro.SnapPro.dto.EquipmentReturnRequest;
import com.event.snappro.SnapPro.service.EquipmentAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment/allocations")
public class EquipmentAllocationController {

    private final EquipmentAllocationService equipmentAllocationService;

    public EquipmentAllocationController(
            EquipmentAllocationService equipmentAllocationService
    ) {
        this.equipmentAllocationService = equipmentAllocationService;
    }


    // =========================================================
    // CHECKOUT EQUIPMENT
    // =========================================================

    @PostMapping("/checkout")
    public ResponseEntity<EquipmentAllocationResponse> checkoutEquipment(
            @RequestBody EquipmentCheckoutRequest request,
            @RequestParam Integer handledBy
    ) {

        EquipmentAllocationResponse response =
                equipmentAllocationService.checkoutEquipment(
                        request,
                        handledBy
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // RETURN EQUIPMENT
    // =========================================================

    @PostMapping("/return")
    public ResponseEntity<EquipmentAllocationResponse> returnEquipment(
            @RequestBody EquipmentReturnRequest request,
            @RequestParam Integer handledBy
    ) {

        EquipmentAllocationResponse response =
                equipmentAllocationService.returnEquipment(
                        request,
                        handledBy
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // REPORT EQUIPMENT DAMAGE
    // =========================================================

    @PostMapping("/damage")
    public ResponseEntity<EquipmentAllocationResponse> reportDamage(
            @RequestBody EquipmentDamageRequest request,
            @RequestParam Integer handledBy
    ) {

        EquipmentAllocationResponse response =
                equipmentAllocationService.reportDamage(
                        request,
                        handledBy
                );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // GET ALL ACTIVE / CHECKED-OUT ALLOCATIONS
    // =========================================================

    @GetMapping("/active")
    public ResponseEntity<List<EquipmentAllocationResponse>>
    getActiveAllocations() {

        return ResponseEntity.ok(
                equipmentAllocationService.getActiveAllocations()
        );
    }


    // =========================================================
    // GET RETURNED ALLOCATIONS
    // =========================================================

    @GetMapping("/returned")
    public ResponseEntity<List<EquipmentAllocationResponse>>
    getReturnedAllocations() {

        return ResponseEntity.ok(
                equipmentAllocationService.getReturnedAllocations()
        );
    }


    // =========================================================
    // GET ALL ALLOCATION HISTORY
    // =========================================================

    @GetMapping
    public ResponseEntity<List<EquipmentAllocationResponse>>
    getAllAllocations() {

        return ResponseEntity.ok(
                equipmentAllocationService.getAllAllocations()
        );
    }


    // =========================================================
    // GET ALLOCATION BY ID
    // =========================================================

    @GetMapping("/{allocationId}")
    public ResponseEntity<EquipmentAllocationResponse>
    getAllocationById(
            @PathVariable Integer allocationId
    ) {

        return ResponseEntity.ok(
                equipmentAllocationService
                        .getAllocationById(allocationId)
        );
    }


    // =========================================================
    // LOOKUP ACTIVE ALLOCATION BY SERIAL NUMBER / BARCODE
    // =========================================================

    @GetMapping("/active/serial/{serialNumber}")
    public ResponseEntity<EquipmentAllocationResponse>
    getActiveAllocationBySerialNumber(
            @PathVariable String serialNumber
    ) {

        return ResponseEntity.ok(
                equipmentAllocationService
                        .getActiveAllocationBySerialNumber(
                                serialNumber
                        )
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY BOOKING
    // =========================================================

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<EquipmentAllocationResponse>>
    getAllocationsByBooking(
            @PathVariable Integer bookingId
    ) {

        return ResponseEntity.ok(
                equipmentAllocationService
                        .getAllocationsByBooking(bookingId)
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY PHOTOGRAPHER
    // =========================================================

    @GetMapping("/photographer/{photographerId}")
    public ResponseEntity<List<EquipmentAllocationResponse>>
    getAllocationsByPhotographer(
            @PathVariable Integer photographerId
    ) {

        return ResponseEntity.ok(
                equipmentAllocationService
                        .getAllocationsByPhotographer(
                                photographerId
                        )
        );
    }
}