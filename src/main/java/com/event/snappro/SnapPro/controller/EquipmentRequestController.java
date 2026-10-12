package com.event.snappro.SnapPro.controller;

import com.event.snappro.SnapPro.dto.EquipmentRequestActionRequest;
import com.event.snappro.SnapPro.dto.EquipmentRequestResponse;
import com.event.snappro.SnapPro.service.EquipmentRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment/requests")
public class EquipmentRequestController {

    private final EquipmentRequestService equipmentRequestService;

    public EquipmentRequestController(
            EquipmentRequestService equipmentRequestService
    ) {
        this.equipmentRequestService = equipmentRequestService;
    }


    // =========================================================
    // GET ALL EQUIPMENT REQUESTS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<EquipmentRequestResponse>>
    getAllRequests() {

        return ResponseEntity.ok(
                equipmentRequestService.getAllRequests()
        );
    }


    // =========================================================
    // GET PENDING REQUESTS
    // =========================================================

    @GetMapping("/pending")
    public ResponseEntity<List<EquipmentRequestResponse>>
    getPendingRequests() {

        return ResponseEntity.ok(
                equipmentRequestService.getPendingRequests()
        );
    }


    // =========================================================
    // GET ONE REQUEST BY ID
    // =========================================================

    @GetMapping("/{requestId}")
    public ResponseEntity<EquipmentRequestResponse>
    getRequestById(
            @PathVariable Integer requestId
    ) {

        return ResponseEntity.ok(
                equipmentRequestService.getRequestById(requestId)
        );
    }


    // =========================================================
    // GET REQUESTS BY BOOKING / EVENT
    // =========================================================

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<EquipmentRequestResponse>>
    getRequestsByBooking(
            @PathVariable Integer bookingId
    ) {

        return ResponseEntity.ok(
                equipmentRequestService
                        .getRequestsByBooking(bookingId)
        );
    }


    // =========================================================
    // GET REQUESTS BY PHOTOGRAPHER
    // =========================================================

    @GetMapping("/photographer/{photographerId}")
    public ResponseEntity<List<EquipmentRequestResponse>>
    getRequestsByPhotographer(
            @PathVariable Integer photographerId
    ) {

        return ResponseEntity.ok(
                equipmentRequestService
                        .getRequestsByPhotographer(
                                photographerId
                        )
        );
    }


    // =========================================================
    // APPROVE OR REJECT EQUIPMENT REQUEST
    // =========================================================

    @PutMapping("/process")
    public ResponseEntity<EquipmentRequestResponse>
    processRequest(
            @RequestBody EquipmentRequestActionRequest request,
            @RequestParam Integer handledBy
    ) {

        EquipmentRequestResponse response =
                equipmentRequestService.processRequest(
                        request,
                        handledBy
                );

        return ResponseEntity.ok(response);
    }
}