package com.event.snappro.SnapPro.service.impl;

import com.event.snappro.SnapPro.dto.EquipmentMaintenanceResponse;
import com.event.snappro.SnapPro.dto.MaintenanceUpdateRequest;
import com.event.snappro.SnapPro.entity.EquipmentInventory;
import com.event.snappro.SnapPro.entity.EquipmentMaintenance;
import com.event.snappro.SnapPro.entity.EquipmentStatus;
import com.event.snappro.SnapPro.repository.EquipmentInventoryRepository;
import com.event.snappro.SnapPro.repository.EquipmentMaintenanceRepository;
import com.event.snappro.SnapPro.repository.EquipmentStatusRepository;
import com.event.snappro.SnapPro.service.EquipmentMaintenanceService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipmentMaintenanceServiceImpl
        implements EquipmentMaintenanceService {

    private final EquipmentMaintenanceRepository equipmentMaintenanceRepository;
    private final EquipmentInventoryRepository equipmentInventoryRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;

    public EquipmentMaintenanceServiceImpl(
            EquipmentMaintenanceRepository equipmentMaintenanceRepository,
            EquipmentInventoryRepository equipmentInventoryRepository,
            EquipmentStatusRepository equipmentStatusRepository
    ) {
        this.equipmentMaintenanceRepository = equipmentMaintenanceRepository;
        this.equipmentInventoryRepository = equipmentInventoryRepository;
        this.equipmentStatusRepository = equipmentStatusRepository;
    }


    // =========================================================
    // GET ALL MAINTENANCE RECORDS
    // =========================================================

    @Override
    public List<EquipmentMaintenanceResponse>
    getAllMaintenanceRecords() {

        return equipmentMaintenanceRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET ACTIVE MAINTENANCE RECORDS
    // =========================================================

    @Override
    public List<EquipmentMaintenanceResponse>
    getActiveMaintenanceRecords() {

        return equipmentMaintenanceRepository
                .findByRepairedAtIsNullOrderByLoggedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET COMPLETED MAINTENANCE RECORDS
    // =========================================================

    @Override
    public List<EquipmentMaintenanceResponse>
    getCompletedMaintenanceRecords() {

        return equipmentMaintenanceRepository
                .findByRepairedAtIsNotNullOrderByRepairedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET MAINTENANCE BY ID
    // =========================================================

    @Override
    public EquipmentMaintenanceResponse getMaintenanceById(
            Integer maintenanceId
    ) {

        EquipmentMaintenance maintenance =
                equipmentMaintenanceRepository
                        .findById(maintenanceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Maintenance record not found with ID: "
                                                + maintenanceId
                                )
                        );

        return mapToResponse(maintenance);
    }


    // =========================================================
    // GET MAINTENANCE HISTORY FOR EQUIPMENT
    // =========================================================

    @Override
    public List<EquipmentMaintenanceResponse>
    getMaintenanceByEquipment(
            Integer equipmentId
    ) {

        if (equipmentId == null) {
            throw new RuntimeException(
                    "Equipment ID is required."
            );
        }

        if (!equipmentInventoryRepository.existsById(equipmentId)) {
            throw new RuntimeException(
                    "Equipment not found with ID: "
                            + equipmentId
            );
        }

        return equipmentMaintenanceRepository
                .findByEquipmentIdOrderByLoggedAtDesc(
                        equipmentId
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // UPDATE MAINTENANCE
    // =========================================================

    @Override
    public EquipmentMaintenanceResponse updateMaintenance(
            MaintenanceUpdateRequest request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Maintenance update request cannot be empty."
            );
        }

        if (request.getMaintenanceId() == null) {
            throw new RuntimeException(
                    "Maintenance ID is required."
            );
        }

        EquipmentMaintenance maintenance =
                equipmentMaintenanceRepository
                        .findById(
                                request.getMaintenanceId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Maintenance record not found with ID: "
                                                + request.getMaintenanceId()
                                )
                        );

        if (maintenance.getRepairedAt() != null) {
            throw new RuntimeException(
                    "This maintenance record has already been completed."
            );
        }

        // -----------------------------------------------------
        // Update maintenance type if supplied
        // -----------------------------------------------------

        if (StringUtils.hasText(
                request.getMaintenanceType()
        )) {
            maintenance.setMaintenanceType(
                    request.getMaintenanceType().trim()
            );
        }

        // -----------------------------------------------------
        // Update cost if supplied
        // -----------------------------------------------------

        if (request.getCost() != null) {

            if (request.getCost()
                    .compareTo(BigDecimal.ZERO) < 0) {

                throw new RuntimeException(
                        "Maintenance cost cannot be negative."
                );
            }

            maintenance.setCost(
                    request.getCost()
            );
        }

        // -----------------------------------------------------
        // Update status if supplied
        // -----------------------------------------------------

        if (StringUtils.hasText(
                request.getStatus()
        )) {

            String newStatus =
                    request.getStatus().trim();

            if ("Repaired".equalsIgnoreCase(newStatus)) {

                return completeMaintenance(
                        maintenance
                );
            }

            maintenance.setStatus(newStatus);
        }

        EquipmentMaintenance saved =
                equipmentMaintenanceRepository
                        .save(maintenance);

        return mapToResponse(saved);
    }


    // =========================================================
    // MARK AS REPAIRED
    // =========================================================

    @Override
    public EquipmentMaintenanceResponse markAsRepaired(
            Integer maintenanceId
    ) {

        if (maintenanceId == null) {
            throw new RuntimeException(
                    "Maintenance ID is required."
            );
        }

        EquipmentMaintenance maintenance =
                equipmentMaintenanceRepository
                        .findById(maintenanceId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Maintenance record not found with ID: "
                                                + maintenanceId
                                )
                        );

        if (maintenance.getRepairedAt() != null) {
            throw new RuntimeException(
                    "This equipment has already been marked as repaired."
            );
        }

        return completeMaintenance(maintenance);
    }


    // =========================================================
    // COMPLETE MAINTENANCE
    // =========================================================

    private EquipmentMaintenanceResponse completeMaintenance(
            EquipmentMaintenance maintenance
    ) {

        EquipmentInventory equipment =
                maintenance.getEquipment();

        if (equipment == null) {
            throw new RuntimeException(
                    "Equipment information is missing for this maintenance record."
            );
        }

        if (equipment.isDecommissioned()) {
            throw new RuntimeException(
                    "Decommissioned equipment cannot be returned to Available status."
            );
        }

        EquipmentStatus availableStatus =
                equipmentStatusRepository
                        .findById(1)
                        .or(() -> equipmentStatusRepository.findByStatus("Available"))
                        .orElseGet(() -> equipmentStatusRepository.save(new EquipmentStatus(1, "Available")));

        // Mark maintenance as complete
        maintenance.setStatus("Repaired");
        maintenance.setRepairedAt(
                LocalDateTime.now()
        );

        // Return equipment to available inventory
        equipment.setEquipmentStatus(
                availableStatus
        );

        equipmentInventoryRepository.save(
                equipment
        );

        EquipmentMaintenance saved =
                equipmentMaintenanceRepository
                        .save(maintenance);

        return mapToResponse(saved);
    }


    // =========================================================
    // ENTITY -> RESPONSE DTO
    // =========================================================

    private EquipmentMaintenanceResponse mapToResponse(
            EquipmentMaintenance maintenance
    ) {

        EquipmentInventory equipment =
                maintenance.getEquipment();

        String rmaReference =
                "#RMA-" + maintenance.getId();

        /*
         * loggedByName is temporarily null because
         * the users table belongs to another module.
         *
         * We will connect the existing users table later
         * without modifying it.
         */

        return new EquipmentMaintenanceResponse(

                maintenance.getId(),

                rmaReference,

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

                maintenance.getIssueDescription(),

                maintenance.getMaintenanceType(),

                maintenance.getCost(),

                maintenance.getLoggedBy(),

                null, // loggedByName

                maintenance.getStatus(),

                maintenance.getLoggedAt(),

                maintenance.getRepairedAt()
        );
    }
}