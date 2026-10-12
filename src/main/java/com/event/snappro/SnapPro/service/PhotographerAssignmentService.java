package com.event.snappro.SnapPro.service;

import com.event.snappro.SnapPro.dto.AssignmentRequestDTO;
import com.event.snappro.SnapPro.dto.ReassignRequestDTO;
import com.event.snappro.SnapPro.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface PhotographerAssignmentService {
    ResponseDTO getAssignableEvents(HttpServletRequest request);
    ResponseDTO getPhotographerAvailability(Integer bookingId, HttpServletRequest request);
    ResponseDTO getAssignmentsForBooking(Integer bookingId, HttpServletRequest request);
    ResponseDTO getRoster(HttpServletRequest request);
    ResponseDTO getPhotographerSchedule(Integer photographerId, HttpServletRequest request);
    ResponseDTO getAllPhotographers(HttpServletRequest request);
    ResponseDTO assign(AssignmentRequestDTO dto, HttpServletRequest request);
    ResponseDTO reassign(Integer assignmentId, ReassignRequestDTO dto, HttpServletRequest request);
    ResponseDTO remove(Integer assignmentId, String reason, HttpServletRequest request);
}

