package com.event.snappro.SnapPro.service.impl;

import com.event.snappro.SnapPro.dto.EquipmentAllocationResponse;
import com.event.snappro.SnapPro.dto.EquipmentCheckoutRequest;
import com.event.snappro.SnapPro.dto.EquipmentDamageRequest;
import com.event.snappro.SnapPro.dto.EquipmentReturnRequest;
import com.event.snappro.SnapPro.entity.EquipmentAllocation;
import com.event.snappro.SnapPro.entity.EquipmentInventory;
import com.event.snappro.SnapPro.entity.EquipmentMaintenance;
import com.event.snappro.SnapPro.entity.EquipmentStatus;
import com.event.snappro.SnapPro.entity.UserEntity;
import com.event.snappro.SnapPro.repository.UserRepository;
import com.event.snappro.SnapPro.repository.EquipmentAllocationRepository;
import com.event.snappro.SnapPro.repository.EquipmentInventoryRepository;
import com.event.snappro.SnapPro.repository.EquipmentMaintenanceRepository;
import com.event.snappro.SnapPro.repository.EquipmentStatusRepository;
import com.event.snappro.SnapPro.entity.EventBookingEntity;
import com.event.snappro.SnapPro.repository.EventBookingRepository;
import com.event.snappro.SnapPro.service.EquipmentAllocationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipmentAllocationServiceImpl
        implements EquipmentAllocationService {

    private final EquipmentAllocationRepository equipmentAllocationRepository;
    private final EquipmentInventoryRepository equipmentInventoryRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;
    private final EquipmentMaintenanceRepository equipmentMaintenanceRepository;
    private final EventBookingRepository eventBookingRepository;
    private final UserRepository userRepository;

    public EquipmentAllocationServiceImpl(
            EquipmentAllocationRepository equipmentAllocationRepository,
            EquipmentInventoryRepository equipmentInventoryRepository,
            EquipmentStatusRepository equipmentStatusRepository,
            EquipmentMaintenanceRepository equipmentMaintenanceRepository,
            EventBookingRepository eventBookingRepository,
            UserRepository userRepository
    ) {
        this.equipmentAllocationRepository = equipmentAllocationRepository;
        this.equipmentInventoryRepository = equipmentInventoryRepository;
        this.equipmentStatusRepository = equipmentStatusRepository;
        this.equipmentMaintenanceRepository = equipmentMaintenanceRepository;
        this.eventBookingRepository = eventBookingRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CHECKOUT EQUIPMENT
    // =========================================================

    @Override
    public EquipmentAllocationResponse checkoutEquipment(
            EquipmentCheckoutRequest request,
            Integer handledBy
    ) {

        if (request == null) {
            throw new RuntimeException("Checkout request cannot be empty.");
        }

        if (request.getEquipmentId() == null) {
            throw new RuntimeException("Equipment ID is required.");
        }

        if (request.getBookingId() == null) {
            throw new RuntimeException("Booking ID is required.");
        }

        if (request.getPhotographerId() == null) {
            throw new RuntimeException("Photographer ID is required.");
        }

        if (handledBy == null) {
            throw new RuntimeException(
                    "Equipment coordinator ID is required."
            );
        }

        if (request.getExpectedReturnTime() != null
                && request.getExpectedReturnTime()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Expected return time cannot be in the past."
            );
        }

        EquipmentInventory equipment =
                equipmentInventoryRepository
                        .findById(request.getEquipmentId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment not found with ID: "
                                                + request.getEquipmentId()
                                )
                        );

        if (equipment.isDecommissioned()) {
            throw new RuntimeException(
                    "Decommissioned equipment cannot be checked out."
            );
        }

        if (equipmentAllocationRepository
                .existsByEquipmentIdAndReturnTimeIsNull(
                        equipment.getId()
                )) {

            throw new RuntimeException(
                    "This equipment is already checked out."
            );
        }

        EquipmentStatus availableStatus =
                getAvailableStatus();

        if (equipment.getEquipmentStatus() == null
                || availableStatus == null
                || !Objects.equals(
                equipment.getEquipmentStatus().getId(),
                availableStatus.getId()
        )) {

            throw new RuntimeException(
                    "Equipment is not currently available for checkout."
            );
        }

        EquipmentStatus checkedOutStatus =
                getCheckedOutStatus();

        EquipmentAllocation allocation =
                new EquipmentAllocation();

        allocation.setEquipment(equipment);
        allocation.setBookingId(request.getBookingId());
        allocation.setPhotographerId(
                request.getPhotographerId()
        );
        allocation.setHandledBy(handledBy);
        allocation.setCheckoutTime(LocalDateTime.now());
        allocation.setExpectedReturnTime(
                request.getExpectedReturnTime()
        );
        allocation.setReportedDamage(false);

        EquipmentAllocation savedAllocation =
                equipmentAllocationRepository.save(allocation);

        equipment.setEquipmentStatus(checkedOutStatus);

        equipmentInventoryRepository.save(equipment);

        return mapToResponse(savedAllocation);
    }


    // =========================================================
    // RECORD EQUIPMENT RETURN
    // =========================================================

    @Override
    public EquipmentAllocationResponse returnEquipment(
            EquipmentReturnRequest request,
            Integer handledBy
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Equipment return request cannot be empty."
            );
        }

        if (request.getAllocationId() == null) {
            throw new RuntimeException(
                    "Allocation ID is required."
            );
        }

        if (handledBy == null) {
            throw new RuntimeException(
                    "Equipment coordinator ID is required."
            );
        }

        EquipmentAllocation allocation =
                equipmentAllocationRepository
                        .findById(request.getAllocationId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Allocation not found with ID: "
                                                + request.getAllocationId()
                                )
                        );

        if (allocation.getReturnTime() != null) {
            throw new RuntimeException(
                    "This equipment has already been returned."
            );
        }

        EquipmentInventory equipment =
                allocation.getEquipment();

        allocation.setReturnTime(LocalDateTime.now());
        allocation.setReturnNotes(request.getReturnNotes());
        allocation.setReportedDamage(
                request.isReportedDamage()
        );

        if (request.isReportedDamage()) {

            String remarks = request.getDamageRemarks();
            if (!StringUtils.hasText(remarks)) {
                remarks = request.getReturnNotes();
            }
            if (!StringUtils.hasText(remarks)) {
                remarks = "Reported damaged during return";
            }
            remarks = remarks.trim();

            allocation.setDamageRemarks(remarks);

            EquipmentStatus maintenanceStatus =
                    getMaintenanceStatus();

            equipment.setEquipmentStatus(
                    maintenanceStatus
            );

            createMaintenanceRecordIfNeeded(
                    equipment,
                    remarks,
                    handledBy
            );

        } else {

            allocation.setDamageRemarks(null);

            EquipmentStatus availableStatus =
                    getAvailableStatus();

            equipment.setEquipmentStatus(
                    availableStatus
            );
        }

        equipmentInventoryRepository.save(equipment);

        EquipmentAllocation savedAllocation =
                equipmentAllocationRepository.save(allocation);

        return mapToResponse(savedAllocation);
    }


    // =========================================================
    // REPORT DAMAGE
    // =========================================================

    @Override
    public EquipmentAllocationResponse reportDamage(
            EquipmentDamageRequest request,
            Integer handledBy
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Damage report cannot be empty."
            );
        }

        if (request.getEquipmentId() == null) {
            throw new RuntimeException(
                    "Equipment ID is required."
            );
        }

        if (handledBy == null) {
            throw new RuntimeException(
                    "Equipment coordinator ID is required."
            );
        }

        if (!StringUtils.hasText(
                request.getDamageDescription()
        )) {
            throw new RuntimeException(
                    "Damage description is required."
            );
        }

        if (!StringUtils.hasText(
                request.getActionRoute()
        )) {
            throw new RuntimeException(
                    "Damage action route is required."
            );
        }

        EquipmentInventory equipment =
                equipmentInventoryRepository
                        .findById(request.getEquipmentId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment not found with ID: "
                                                + request.getEquipmentId()
                                )
                        );

        EquipmentAllocation allocation;

        if (request.getAllocationId() != null) {

            allocation =
                    equipmentAllocationRepository
                            .findById(
                                    request.getAllocationId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Allocation not found with ID: "
                                                    + request.getAllocationId()
                                    )
                            );

        } else {

            allocation =
                    equipmentAllocationRepository
                            .findFirstByEquipmentIdAndReturnTimeIsNullOrderByCheckoutTimeDesc(
                                    equipment.getId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "No active allocation found for this equipment."
                                    )
                            );
        }

        if (!Objects.equals(
                allocation.getEquipment().getId(),
                equipment.getId()
        )) {

            throw new RuntimeException(
                    "The allocation does not belong to the selected equipment."
            );
        }

        if (allocation.getReturnTime() != null) {
            throw new RuntimeException(
                    "The selected allocation is already closed."
            );
        }

        allocation.setReportedDamage(true);
        allocation.setDamageRemarks(
                request.getDamageDescription()
        );

        // Reporting damage through this workflow removes the
        // equipment from the active checkout roster.
        allocation.setReturnTime(LocalDateTime.now());

        String route =
                request.getActionRoute()
                        .trim()
                        .toUpperCase();

        switch (route) {

            case "REPAIR" -> {

                EquipmentStatus maintenanceStatus =
                        getMaintenanceStatus();

                equipment.setEquipmentStatus(
                        maintenanceStatus
                );

                createMaintenanceRecordIfNeeded(
                        equipment,
                        request.getDamageDescription(),
                        handledBy
                );
            }

            case "DECOMMISSION" -> {

                EquipmentStatus decommissionedStatus =
                        getDecommissionedStatus();

                equipment.setEquipmentStatus(
                        decommissionedStatus
                );

                equipment.setDecommissioned(true);
            }

            default -> throw new RuntimeException(
                    "Invalid action route. Use REPAIR or DECOMMISSION."
            );
        }

        equipmentInventoryRepository.save(equipment);

        EquipmentAllocation savedAllocation =
                equipmentAllocationRepository.save(allocation);

        return mapToResponse(savedAllocation);
    }


    // =========================================================
    // GET ACTIVE ALLOCATIONS
    // =========================================================

    @Override
    public List<EquipmentAllocationResponse>
    getActiveAllocations() {

        return equipmentAllocationRepository
                .findByReturnTimeIsNullOrderByCheckoutTimeDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET RETURNED ALLOCATIONS
    // =========================================================

    @Override
    public List<EquipmentAllocationResponse>
    getReturnedAllocations() {

        return equipmentAllocationRepository
                .findByReturnTimeIsNotNullOrderByReturnTimeDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET ALL ALLOCATIONS
    // =========================================================

    @Override
    public List<EquipmentAllocationResponse>
    getAllAllocations() {

        return equipmentAllocationRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET ALLOCATION BY ID
    // =========================================================

    @Override
    public EquipmentAllocationResponse getAllocationById(
            Integer allocationId
    ) {

        EquipmentAllocation allocation =
                equipmentAllocationRepository
                        .findById(allocationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Allocation not found with ID: "
                                                + allocationId
                                )
                        );

        return mapToResponse(allocation);
    }


    // =========================================================
    // FIND ACTIVE ALLOCATION USING SERIAL NUMBER
    // =========================================================

    @Override
    public EquipmentAllocationResponse
    getActiveAllocationBySerialNumber(
            String serialNumber
    ) {

        if (!StringUtils.hasText(serialNumber)) {
            throw new RuntimeException(
                    "Serial number is required."
            );
        }

        EquipmentInventory equipment =
                equipmentInventoryRepository
                        .findBySerialNumber(
                                serialNumber.trim()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment not found with serial number: "
                                                + serialNumber
                                )
                        );

        EquipmentAllocation allocation =
                equipmentAllocationRepository
                        .findFirstByEquipmentIdAndReturnTimeIsNullOrderByCheckoutTimeDesc(
                                equipment.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active checkout found for equipment: "
                                                + serialNumber
                                )
                        );

        return mapToResponse(allocation);
    }


    // =========================================================
    // ALLOCATIONS BY BOOKING
    // =========================================================

    @Override
    public List<EquipmentAllocationResponse>
    getAllocationsByBooking(
            Integer bookingId
    ) {

        return equipmentAllocationRepository
                .findByBookingIdOrderByCheckoutTimeDesc(
                        bookingId
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // ALLOCATIONS BY PHOTOGRAPHER
    // =========================================================

    @Override
    public List<EquipmentAllocationResponse>
    getAllocationsByPhotographer(
            Integer photographerId
    ) {

        return equipmentAllocationRepository
                .findByPhotographerIdOrderByCheckoutTimeDesc(
                        photographerId
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // CREATE MAINTENANCE RECORD IF ONE IS NOT ACTIVE
    // =========================================================

    private void createMaintenanceRecordIfNeeded(
            EquipmentInventory equipment,
            String damageDescription,
            Integer loggedBy
    ) {
        if (equipment == null) {
            return;
        }

        EquipmentMaintenance maintenance =
                new EquipmentMaintenance();

        maintenance.setEquipment(equipment);
        maintenance.setIssueDescription(
                StringUtils.hasText(damageDescription)
                        ? damageDescription.trim()
                        : "Reported damaged during return"
        );
        maintenance.setMaintenanceType("Repair");
        maintenance.setCost(BigDecimal.ZERO);
        Integer coordinatorId = (loggedBy != null) ? loggedBy : 1;
        maintenance.setLoggedBy(coordinatorId);
        maintenance.setReportedBy(coordinatorId);
        maintenance.setStatus("In Maintenance");
        maintenance.setLoggedAt(LocalDateTime.now());

        equipmentMaintenanceRepository.save(
                maintenance
        );
    }


    // =========================================================
    // SAFE STATUS LOOKUPS (ID 1 = Available, ID 2 = Checked Out, ID 3 = In Maintenance, ID 4 = Decommissioned)
    // =========================================================

    private EquipmentStatus getAvailableStatus() {
        return getStatusByIdOrName(1, "Available");
    }

    private EquipmentStatus getCheckedOutStatus() {
        return getStatusByIdOrName(2, "Checked Out");
    }

    private EquipmentStatus getMaintenanceStatus() {
        return getStatusByIdOrName(3, "In Maintenance");
    }

    private EquipmentStatus getDecommissionedStatus() {
        return getStatusByIdOrName(4, "Decommissioned");
    }

    private EquipmentStatus getStatusByName(
            String statusName
    ) {
        if (!StringUtils.hasText(statusName)) {
            return getAvailableStatus();
        }
        String trimmed = statusName.trim();
        if ("Available".equalsIgnoreCase(trimmed)) {
            return getAvailableStatus();
        } else if ("Checked Out".equalsIgnoreCase(trimmed)) {
            return getCheckedOutStatus();
        } else if ("In Maintenance".equalsIgnoreCase(trimmed) || "Maintenance".equalsIgnoreCase(trimmed)) {
            return getMaintenanceStatus();
        } else if ("Decommissioned".equalsIgnoreCase(trimmed)) {
            return getDecommissionedStatus();
        }
        return getStatusByIdOrName(null, trimmed);
    }

    private EquipmentStatus getStatusByIdOrName(
            Integer id,
            String preferredName
    ) {
        // 1. Check ID in repository
        if (id != null) {
            java.util.Optional<EquipmentStatus> byId =
                    equipmentStatusRepository.findById(id);
            if (byId.isPresent()) {
                return byId.get();
            }
        }

        // 2. Check preferred name
        if (StringUtils.hasText(preferredName)) {
            java.util.Optional<EquipmentStatus> byName =
                    equipmentStatusRepository.findByStatus(preferredName.trim());
            if (byName.isPresent()) {
                return byName.get();
            }

            // 3. Check common naming variations
            if ("In Maintenance".equalsIgnoreCase(preferredName)
                    || "Maintenance".equalsIgnoreCase(preferredName)) {
                java.util.Optional<EquipmentStatus> alias =
                        equipmentStatusRepository.findByStatus("Maintenance");
                if (alias.isPresent()) {
                    return alias.get();
                }
            }
        }

        // 4. Safe fallback: persist status to avoid NullPointerException or missing record error
        EquipmentStatus fallback = new EquipmentStatus();
        if (id != null) {
            fallback.setId(id);
        }
        fallback.setStatus(StringUtils.hasText(preferredName) ? preferredName.trim() : "Available");
        return equipmentStatusRepository.save(fallback);
    }


    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private EquipmentAllocationResponse mapToResponse(
            EquipmentAllocation allocation
    ) {

        EquipmentInventory equipment =
                allocation.getEquipment();

        String status = null;

        if (equipment != null
                && equipment.getEquipmentStatus() != null) {

            status =
                    equipment
                            .getEquipmentStatus()
                            .getStatus();
        }

        // Resolve event name from EventBookingRepository
        String eventName = null;
        if (allocation.getBookingId() != null && eventBookingRepository != null) {
            try {
                eventName = eventBookingRepository.findById(allocation.getBookingId())
                        .map(EventBookingEntity::getEventTitle)
                        .orElse(null);
            } catch (Exception ignored) {
            }
        }

        if (eventName == null) {
            if (allocation.getBookingId() != null) {
                if (allocation.getBookingId() == 1) {
                    eventName = "Corporate Annual Gala";
                } else if (allocation.getBookingId() == 2) {
                    eventName = "Colombo Fashion Week";
                } else if (allocation.getBookingId() == 3) {
                    eventName = "Tech Leaders Summit";
                } else {
                    eventName = "Event #" + allocation.getBookingId();
                }
            } else {
                eventName = "Event";
            }
        }

        // Resolve photographer full name from UserRepository (reads users table, no schema change)
        String photographerName = null;
        if (allocation.getPhotographerId() != null && userRepository != null) {
            try {
                photographerName = userRepository.findById(allocation.getPhotographerId())
                        .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                        .orElse(null);
            } catch (Exception ignored) {
            }
        }
        if (photographerName == null || photographerName.isBlank()) {
            photographerName = allocation.getPhotographerId() != null
                    ? "Photographer #" + allocation.getPhotographerId()
                    : "Photographer";
        }

        return new EquipmentAllocationResponse(
                allocation.getId(),

                equipment != null
                        ? equipment.getId()
                        : null,

                equipment != null
                        ? equipment.getSerialNumber()
                        : null,

                equipment != null
                        ? equipment.getItemName()
                        : null,

                equipment != null
                        ? equipment.getModel()
                        : null,

                allocation.getBookingId(),

                eventName, // bookingReference

                eventName, // eventName

                allocation.getPhotographerId(),

                photographerName, // resolved from users table

                allocation.getHandledBy(),

                null, // handledByName

                allocation.getCheckoutTime(),

                allocation.getExpectedReturnTime(),

                allocation.getReturnTime(),

                allocation.getReturnNotes(),

                allocation.isReportedDamage(),

                allocation.getDamageRemarks(),

                status
        );
    }
}