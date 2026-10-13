package com.event.snappro.SnapPro.service;

import com.event.snappro.SnapPro.dto.EquipmentAllocationResponse;
import com.event.snappro.SnapPro.dto.EquipmentCheckoutRequest;
import com.event.snappro.SnapPro.dto.EquipmentDamageRequest;
import com.event.snappro.SnapPro.dto.EquipmentReturnRequest;

import java.util.List;

public interface EquipmentAllocationService {

    EquipmentAllocationResponse checkoutEquipment(
            EquipmentCheckoutRequest request,
            Integer handledBy
    );

    EquipmentAllocationResponse returnEquipment(
            EquipmentReturnRequest request,
            Integer handledBy
    );

    EquipmentAllocationResponse reportDamage(
            EquipmentDamageRequest request,
            Integer handledBy
    );

    List<EquipmentAllocationResponse> getActiveAllocations();

    List<EquipmentAllocationResponse> getReturnedAllocations();

    List<EquipmentAllocationResponse> getAllAllocations();

    EquipmentAllocationResponse getAllocationById(
            Integer allocationId
    );

    EquipmentAllocationResponse getActiveAllocationBySerialNumber(
            String serialNumber
    );

    List<EquipmentAllocationResponse> getAllocationsByBooking(
            Integer bookingId
    );

    List<EquipmentAllocationResponse> getAllocationsByPhotographer(
            Integer photographerId
    );
}