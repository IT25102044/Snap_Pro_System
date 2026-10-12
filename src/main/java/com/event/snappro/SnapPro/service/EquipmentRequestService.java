package com.event.snappro.SnapPro.service;

import com.event.snappro.SnapPro.dto.EquipmentRequestActionRequest;
import com.event.snappro.SnapPro.dto.EquipmentRequestResponse;

import java.util.List;

public interface EquipmentRequestService {

    // Get all equipment requests
    List<EquipmentRequestResponse> getAllRequests();

    // Get only pending requests
    List<EquipmentRequestResponse> getPendingRequests();

    // Get requests for one booking/event
    List<EquipmentRequestResponse> getRequestsByBooking(
            Integer bookingId
    );

    // Get requests made by one photographer
    List<EquipmentRequestResponse> getRequestsByPhotographer(
            Integer photographerId
    );

    // Get one request by ID
    EquipmentRequestResponse getRequestById(
            Integer requestId
    );

    // Approve or reject a request
    EquipmentRequestResponse processRequest(
            EquipmentRequestActionRequest request,
            Integer handledBy
    );
}