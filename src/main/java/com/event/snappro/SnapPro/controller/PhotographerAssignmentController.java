package com.event.snappro.SnapPro.controller;

import com.event.snappro.SnapPro.dto.AssignmentRequestDTO;
import com.event.snappro.SnapPro.dto.ReassignRequestDTO;
import com.event.snappro.SnapPro.dto.ResponseDTO;
import com.event.snappro.SnapPro.service.PhotographerAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ops/assignments")
@RequiredArgsConstructor
public class PhotographerAssignmentController {

    private final PhotographerAssignmentService service;

    @GetMapping("/events")
    public ResponseEntity<ResponseDTO> assignableEvents(HttpServletRequest request) {
        return respond(service.getAssignableEvents(request));
    }

    @GetMapping("/events/{bookingId}/photographers")
    public ResponseEntity<ResponseDTO> availability(@PathVariable Integer bookingId, HttpServletRequest request) {
        return respond(service.getPhotographerAvailability(bookingId, request));
    }

    @GetMapping("/events/{bookingId}")
    public ResponseEntity<ResponseDTO> forBooking(@PathVariable Integer bookingId, HttpServletRequest request) {
        return respond(service.getAssignmentsForBooking(bookingId, request));
    }

    @GetMapping("/roster")
    public ResponseEntity<ResponseDTO> roster(HttpServletRequest request) {
        return respond(service.getRoster(request));
    }

    @GetMapping("/photographers/{photographerId}/schedule")
    public ResponseEntity<ResponseDTO> schedule(@PathVariable Integer photographerId, HttpServletRequest request) {
        return respond(service.getPhotographerSchedule(photographerId, request));
    }

    @GetMapping("/photographers")
    public ResponseEntity<ResponseDTO> photographers(HttpServletRequest request) {
        return respond(service.getAllPhotographers(request));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> assign(@RequestBody AssignmentRequestDTO dto, HttpServletRequest request) {
        return respond(service.assign(dto, request));
    }

    @PutMapping("/{id}/reassign")
    public ResponseEntity<ResponseDTO> reassign(@PathVariable Integer id, @RequestBody ReassignRequestDTO dto,
                                                HttpServletRequest request) {
        return respond(service.reassign(id, dto, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> remove(@PathVariable Integer id, @RequestParam String reason,
                                              HttpServletRequest request) {
        return respond(service.remove(id, reason, request));
    }

    private ResponseEntity<ResponseDTO> respond(ResponseDTO r) {
        return r.isStatus() ? ResponseEntity.ok(r) : ResponseEntity.badRequest().body(r);
    }
}

