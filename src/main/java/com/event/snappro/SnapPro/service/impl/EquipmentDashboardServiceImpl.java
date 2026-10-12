package com.event.snappro.SnapPro.service.impl;

import com.event.snappro.SnapPro.dto.EquipmentDashboardResponse;
import com.event.snappro.SnapPro.entity.EquipmentStatus;
import com.event.snappro.SnapPro.repository.EquipmentAllocationRepository;
import com.event.snappro.SnapPro.repository.EquipmentInventoryRepository;
import com.event.snappro.SnapPro.repository.EquipmentStatusRepository;
import com.event.snappro.SnapPro.service.EquipmentDashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class EquipmentDashboardServiceImpl
        implements EquipmentDashboardService {

    private final EquipmentInventoryRepository equipmentInventoryRepository;
    private final EquipmentAllocationRepository equipmentAllocationRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;

    public EquipmentDashboardServiceImpl(
            EquipmentInventoryRepository equipmentInventoryRepository,
            EquipmentAllocationRepository equipmentAllocationRepository,
            EquipmentStatusRepository equipmentStatusRepository
    ) {
        this.equipmentInventoryRepository = equipmentInventoryRepository;
        this.equipmentAllocationRepository = equipmentAllocationRepository;
        this.equipmentStatusRepository = equipmentStatusRepository;
    }

    @Override
    public EquipmentDashboardResponse getDashboardSummary() {

        // -----------------------------------------------------
        // Find equipment status records
        // -----------------------------------------------------

        EquipmentStatus availableStatus =
                getAvailableStatus();

        EquipmentStatus checkedOutStatus =
                getCheckedOutStatus();

        EquipmentStatus maintenanceStatus =
                getMaintenanceStatus();


        // -----------------------------------------------------
        // AVAILABLE EQUIPMENT
        // -----------------------------------------------------

        long availableItems =
                equipmentInventoryRepository
                        .countByEquipmentStatusIdAndDecommissionedFalse(
                                availableStatus.getId()
                        );


        // -----------------------------------------------------
        // CHECKED OUT EQUIPMENT
        // -----------------------------------------------------

        long checkedOutItems =
                equipmentInventoryRepository
                        .countByEquipmentStatusIdAndDecommissionedFalse(
                                checkedOutStatus.getId()
                        );


        // -----------------------------------------------------
        // EQUIPMENT CURRENTLY IN MAINTENANCE
        // -----------------------------------------------------

        long maintenanceItems =
                equipmentInventoryRepository
                        .countByEquipmentStatusIdAndDecommissionedFalse(
                                maintenanceStatus.getId()
                        );


        // -----------------------------------------------------
        // EXPECTED RETURNS TODAY
        // -----------------------------------------------------

        LocalDate today = LocalDate.now();

        LocalDateTime startOfToday =
                today.atStartOfDay();

        LocalDateTime endOfToday =
                today.atTime(LocalTime.MAX);

        long expectedReturnsToday =
                equipmentAllocationRepository
                        .countByExpectedReturnTimeBetweenAndReturnTimeIsNull(
                                startOfToday,
                                endOfToday
                        );


        // -----------------------------------------------------
        // BUILD RESPONSE
        // -----------------------------------------------------

        return new EquipmentDashboardResponse(
                availableItems,
                checkedOutItems,
                expectedReturnsToday,
                maintenanceItems
        );
    }


    // =========================================================
    // SAFE STATUS LOOKUPS
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

    private EquipmentStatus getStatusByName(
            String statusName
    ) {
        if (!org.springframework.util.StringUtils.hasText(statusName)) {
            return getAvailableStatus();
        }
        String trimmed = statusName.trim();
        if ("Available".equalsIgnoreCase(trimmed)) {
            return getAvailableStatus();
        } else if ("Checked Out".equalsIgnoreCase(trimmed)) {
            return getCheckedOutStatus();
        } else if ("In Maintenance".equalsIgnoreCase(trimmed) || "Maintenance".equalsIgnoreCase(trimmed)) {
            return getMaintenanceStatus();
        }
        return getStatusByIdOrName(null, trimmed);
    }

    private EquipmentStatus getStatusByIdOrName(
            Integer id,
            String preferredName
    ) {
        if (id != null) {
            java.util.Optional<EquipmentStatus> byId =
                    equipmentStatusRepository.findById(id);
            if (byId.isPresent()) {
                return byId.get();
            }
        }

        if (org.springframework.util.StringUtils.hasText(preferredName)) {
            java.util.Optional<EquipmentStatus> byName =
                    equipmentStatusRepository.findByStatus(preferredName.trim());
            if (byName.isPresent()) {
                return byName.get();
            }

            if ("In Maintenance".equalsIgnoreCase(preferredName)
                    || "Maintenance".equalsIgnoreCase(preferredName)) {
                java.util.Optional<EquipmentStatus> alias =
                        equipmentStatusRepository.findByStatus("Maintenance");
                if (alias.isPresent()) {
                    return alias.get();
                }
            }
        }

        EquipmentStatus fallback = new EquipmentStatus();
        if (id != null) {
            fallback.setId(id);
        }
        fallback.setStatus(org.springframework.util.StringUtils.hasText(preferredName) ? preferredName.trim() : "Available");
        return equipmentStatusRepository.save(fallback);
    }
}