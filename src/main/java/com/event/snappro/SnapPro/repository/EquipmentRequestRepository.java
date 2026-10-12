package com.event.snappro.SnapPro.repository;

import com.event.snappro.SnapPro.entity.EquipmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRequestRepository
        extends JpaRepository<EquipmentRequest, Integer> {

    // Pending requests for the equipment coordinator
    List<EquipmentRequest>
    findByRequestStatusIgnoreCaseOrderByRequestedAtAsc(
            String requestStatus
    );

    // Requests for a specific booking/event
    List<EquipmentRequest>
    findByBookingIdOrderByRequestedAtDesc(
            Integer bookingId
    );

    // Requests made by a specific photographer
    List<EquipmentRequest>
    findByPhotographerIdOrderByRequestedAtDesc(
            Integer photographerId
    );

    // Requests handled by a specific coordinator
    List<EquipmentRequest>
    findByHandledByOrderByRequestedAtDesc(
            Integer handledBy
    );

    // Search pending requests by urgency
    List<EquipmentRequest>
    findByRequestStatusIgnoreCaseAndUrgencyLevelIgnoreCaseOrderByRequestedAtAsc(
            String requestStatus,
            String urgencyLevel
    );

    // Count pending requests
    long countByRequestStatusIgnoreCase(
            String requestStatus
    );

    // Check whether a request has already been fulfilled
    boolean existsByIdAndFulfilledAllocationIsNotNull(
            Integer id
    );
}