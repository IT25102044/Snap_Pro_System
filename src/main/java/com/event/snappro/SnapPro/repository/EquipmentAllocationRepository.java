package com.event.snappro.SnapPro.repository;

import com.event.snappro.SnapPro.entity.EquipmentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentAllocationRepository
        extends JpaRepository<EquipmentAllocation, Integer> {

    // Active allocation for a specific equipment item
    Optional<EquipmentAllocation>
    findFirstByEquipmentIdAndReturnTimeIsNullOrderByCheckoutTimeDesc(
            Integer equipmentId
    );

    // All currently checked-out equipment
    List<EquipmentAllocation>
    findByReturnTimeIsNullOrderByCheckoutTimeDesc();

    // Allocation history for a specific equipment item
    List<EquipmentAllocation>
    findByEquipmentIdOrderByCheckoutTimeDesc(
            Integer equipmentId
    );

    // Allocation history for a booking/event
    List<EquipmentAllocation>
    findByBookingIdOrderByCheckoutTimeDesc(
            Integer bookingId
    );

    // Allocation history for a photographer
    List<EquipmentAllocation>
    findByPhotographerIdOrderByCheckoutTimeDesc(
            Integer photographerId
    );

    // Expected returns within a date/time range
    List<EquipmentAllocation>
    findByExpectedReturnTimeBetweenAndReturnTimeIsNull(
            LocalDateTime start,
            LocalDateTime end
    );

    // Dashboard count for expected returns
    long countByExpectedReturnTimeBetweenAndReturnTimeIsNull(
            LocalDateTime start,
            LocalDateTime end
    );

    // Check whether equipment is currently allocated
    boolean existsByEquipmentIdAndReturnTimeIsNull(
            Integer equipmentId
    );

    // Returned allocation history
    List<EquipmentAllocation>
    findByReturnTimeIsNotNullOrderByReturnTimeDesc();

    // Damaged returned equipment history
    List<EquipmentAllocation>
    findByReportedDamageTrueOrderByCheckoutTimeDesc();
}