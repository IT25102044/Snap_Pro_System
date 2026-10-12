package com.event.snappro.SnapPro.service.impl;

import com.event.snappro.SnapPro.dto.EquipmentRequestActionRequest;
import com.event.snappro.SnapPro.dto.EquipmentRequestResponse;
import com.event.snappro.SnapPro.entity.EquipmentAllocation;
import com.event.snappro.SnapPro.entity.EquipmentInventory;
import com.event.snappro.SnapPro.entity.EquipmentRequest;
import com.event.snappro.SnapPro.entity.EquipmentStatus;
import com.event.snappro.SnapPro.repository.EquipmentAllocationRepository;
import com.event.snappro.SnapPro.repository.EquipmentInventoryRepository;
import com.event.snappro.SnapPro.repository.EquipmentRequestRepository;
import com.event.snappro.SnapPro.repository.EquipmentStatusRepository;
import com.event.snappro.SnapPro.service.EquipmentRequestService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipmentRequestServiceImpl
        implements EquipmentRequestService {

    private final EquipmentRequestRepository equipmentRequestRepository;
    private final EquipmentInventoryRepository equipmentInventoryRepository;
    private final EquipmentAllocationRepository equipmentAllocationRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;

    public EquipmentRequestServiceImpl(
            EquipmentRequestRepository equipmentRequestRepository,
            EquipmentInventoryRepository equipmentInventoryRepository,
            EquipmentAllocationRepository equipmentAllocationRepository,
            EquipmentStatusRepository equipmentStatusRepository
    ) {
        this.equipmentRequestRepository = equipmentRequestRepository;
        this.equipmentInventoryRepository = equipmentInventoryRepository;
        this.equipmentAllocationRepository = equipmentAllocationRepository;
        this.equipmentStatusRepository = equipmentStatusRepository;
    }


    // =========================================================
    // GET ALL REQUESTS
    // =========================================================

    @Override
    public List<EquipmentRequestResponse> getAllRequests() {

        return equipmentRequestRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET PENDING REQUESTS
    // =========================================================

    @Override
    public List<EquipmentRequestResponse> getPendingRequests() {

        return equipmentRequestRepository
                .findByRequestStatusIgnoreCaseOrderByRequestedAtAsc(
                        "Pending"
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET REQUESTS BY BOOKING
    // =========================================================

    @Override
    public List<EquipmentRequestResponse> getRequestsByBooking(
            Integer bookingId
    ) {

        if (bookingId == null) {
            throw new RuntimeException(
                    "Booking ID is required."
            );
        }

        return equipmentRequestRepository
                .findByBookingIdOrderByRequestedAtDesc(
                        bookingId
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET REQUESTS BY PHOTOGRAPHER
    // =========================================================

    @Override
    public List<EquipmentRequestResponse> getRequestsByPhotographer(
            Integer photographerId
    ) {

        if (photographerId == null) {
            throw new RuntimeException(
                    "Photographer ID is required."
            );
        }

        return equipmentRequestRepository
                .findByPhotographerIdOrderByRequestedAtDesc(
                        photographerId
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET REQUEST BY ID
    // =========================================================

    @Override
    public EquipmentRequestResponse getRequestById(
            Integer requestId
    ) {

        if (requestId == null) {
            throw new RuntimeException(
                    "Request ID is required."
            );
        }

        EquipmentRequest request =
                equipmentRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment request not found with ID: "
                                                + requestId
                                )
                        );

        return mapToResponse(request);
    }


    // =========================================================
    // APPROVE / REJECT REQUEST
    // =========================================================

    @Override
    public EquipmentRequestResponse processRequest(
            EquipmentRequestActionRequest actionRequest,
            Integer handledBy
    ) {

        if (actionRequest == null) {
            throw new RuntimeException(
                    "Request action cannot be empty."
            );
        }

        if (actionRequest.getRequestId() == null) {
            throw new RuntimeException(
                    "Request ID is required."
            );
        }

        if (handledBy == null) {
            throw new RuntimeException(
                    "Equipment coordinator ID is required."
            );
        }

        if (!StringUtils.hasText(
                actionRequest.getAction()
        )) {
            throw new RuntimeException(
                    "Action is required."
            );
        }

        EquipmentRequest equipmentRequest =
                equipmentRequestRepository
                        .findById(
                                actionRequest.getRequestId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment request not found with ID: "
                                                + actionRequest.getRequestId()
                                )
                        );

        // Prevent the same request from being processed twice
        if (!"Pending".equalsIgnoreCase(
                equipmentRequest.getRequestStatus()
        )) {
            throw new RuntimeException(
                    "This equipment request has already been processed."
            );
        }

        String action =
                actionRequest
                        .getAction()
                        .trim()
                        .toUpperCase();

        return switch (action) {

            case "APPROVE" ->
                    approveRequest(
                            equipmentRequest,
                            actionRequest,
                            handledBy
                    );

            case "REJECT" ->
                    rejectRequest(
                            equipmentRequest,
                            actionRequest,
                            handledBy
                    );

            default ->
                    throw new RuntimeException(
                            "Invalid action. Use APPROVE or REJECT."
                    );
        };
    }


    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    private EquipmentRequestResponse approveRequest(
            EquipmentRequest equipmentRequest,
            EquipmentRequestActionRequest actionRequest,
            Integer handledBy
    ) {

        if (actionRequest.getEquipmentId() == null) {
            throw new RuntimeException(
                    "Equipment ID is required when approving a request."
            );
        }

        if (actionRequest.getExpectedReturnTime() == null) {
            throw new RuntimeException(
                    "Expected return time is required when approving a request."
            );
        }

        if (actionRequest
                .getExpectedReturnTime()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Expected return time cannot be in the past."
            );
        }

        EquipmentInventory equipment =
                equipmentInventoryRepository
                        .findById(
                                actionRequest.getEquipmentId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment not found with ID: "
                                                + actionRequest.getEquipmentId()
                                )
                        );

        if (equipment.isDecommissioned()) {
            throw new RuntimeException(
                    "Decommissioned equipment cannot be dispatched."
            );
        }

        EquipmentStatus availableStatus =
                getStatusByName("Available");

        if (equipment.getEquipmentStatus() == null
                || !Objects.equals(
                equipment.getEquipmentStatus().getId(),
                availableStatus.getId()
        )) {

            throw new RuntimeException(
                    "Selected equipment is not available."
            );
        }

        boolean currentlyAllocated =
                equipmentAllocationRepository
                        .existsByEquipmentIdAndReturnTimeIsNull(
                                equipment.getId()
                        );

        if (currentlyAllocated) {
            throw new RuntimeException(
                    "Selected equipment is already checked out."
            );
        }

        EquipmentStatus checkedOutStatus =
                getStatusByName("Checked Out");


        // -----------------------------------------------------
        // Create the actual equipment allocation
        // -----------------------------------------------------

        EquipmentAllocation allocation =
                new EquipmentAllocation();

        allocation.setEquipment(equipment);

        allocation.setBookingId(
                equipmentRequest.getBookingId()
        );

        allocation.setPhotographerId(
                equipmentRequest.getPhotographerId()
        );

        allocation.setHandledBy(
                handledBy
        );

        allocation.setCheckoutTime(
                LocalDateTime.now()
        );

        allocation.setExpectedReturnTime(
                actionRequest.getExpectedReturnTime()
        );

        allocation.setReportedDamage(false);

        EquipmentAllocation savedAllocation =
                equipmentAllocationRepository
                        .save(allocation);


        // -----------------------------------------------------
        // Change inventory status to Checked Out
        // -----------------------------------------------------

        equipment.setEquipmentStatus(
                checkedOutStatus
        );

        equipmentInventoryRepository.save(
                equipment
        );


        // -----------------------------------------------------
        // Update original equipment request
        // -----------------------------------------------------

        equipmentRequest.setRequestStatus(
                "Approved"
        );

        equipmentRequest.setHandledBy(
                handledBy
        );

        equipmentRequest.setCoordinatorNotes(
                actionRequest.getCoordinatorNotes()
        );

        equipmentRequest.setResolvedAt(
                LocalDateTime.now()
        );

        equipmentRequest.setFulfilledAllocation(
                savedAllocation
        );

        EquipmentRequest savedRequest =
                equipmentRequestRepository
                        .save(equipmentRequest);

        return mapToResponse(savedRequest);
    }


    // =========================================================
    // REJECT REQUEST
    // =========================================================

    private EquipmentRequestResponse rejectRequest(
            EquipmentRequest equipmentRequest,
            EquipmentRequestActionRequest actionRequest,
            Integer handledBy
    ) {

        if (!StringUtils.hasText(
                actionRequest.getCoordinatorNotes()
        )) {
            throw new RuntimeException(
                    "Please provide a reason for rejecting the request."
            );
        }

        equipmentRequest.setRequestStatus(
                "Rejected"
        );

        equipmentRequest.setHandledBy(
                handledBy
        );

        equipmentRequest.setCoordinatorNotes(
                actionRequest
                        .getCoordinatorNotes()
                        .trim()
        );

        equipmentRequest.setResolvedAt(
                LocalDateTime.now()
        );

        EquipmentRequest savedRequest =
                equipmentRequestRepository
                        .save(equipmentRequest);

        return mapToResponse(savedRequest);
    }


    // =========================================================
    // GET EQUIPMENT STATUS
    // =========================================================

    private EquipmentStatus getStatusByName(
            String statusName
    ) {

        return equipmentStatusRepository
                .findByStatus(statusName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Equipment status not found: "
                                        + statusName
                        )
                );
    }


    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private EquipmentRequestResponse mapToResponse(
            EquipmentRequest request
    ) {

        Integer fulfilledAllocationId = null;

        if (request.getFulfilledAllocation() != null) {
            fulfilledAllocationId =
                    request
                            .getFulfilledAllocation()
                            .getId();
        }

        /*
         * bookingReference,
         * photographerName,
         * handledByName
         *
         * remain null for now because the related
         * event_bookings / photographer_profiles / users
         * backend entities belong to the other modules.
         */

        return new EquipmentRequestResponse(

                request.getId(),

                request.getBookingId(),

                null, // bookingReference

                request.getPhotographerId(),

                null, // photographerName

                request.getRequestedItemType(),

                request.getUrgencyLevel(),

                request.getRequestNotes(),

                request.getRequestStatus(),

                request.getRequestedAt(),

                request.getHandledBy(),

                null, // handledByName

                request.getCoordinatorNotes(),

                request.getResolvedAt(),

                fulfilledAllocationId
        );
    }
}