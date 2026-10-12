package com.event.snappro.SnapPro.service.impl;

import com.event.snappro.SnapPro.dto.EquipmentInventoryRequest;
import com.event.snappro.SnapPro.dto.EquipmentInventoryResponse;
import com.event.snappro.SnapPro.entity.EquipmentInventory;
import com.event.snappro.SnapPro.entity.EquipmentStatus;
import com.event.snappro.SnapPro.repository.EquipmentInventoryRepository;
import com.event.snappro.SnapPro.repository.EquipmentStatusRepository;
import com.event.snappro.SnapPro.service.EquipmentInventoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipmentInventoryServiceImpl
        implements EquipmentInventoryService {

    private final EquipmentInventoryRepository equipmentInventoryRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;

    public EquipmentInventoryServiceImpl(
            EquipmentInventoryRepository equipmentInventoryRepository,
            EquipmentStatusRepository equipmentStatusRepository
    ) {
        this.equipmentInventoryRepository = equipmentInventoryRepository;
        this.equipmentStatusRepository = equipmentStatusRepository;
    }

    @Override
    public List<EquipmentInventoryResponse> getAllEquipment() {

        return equipmentInventoryRepository
                .findByDecommissionedFalse()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipmentInventoryResponse> getAvailableEquipment() {

        // Equipment Status ID 1 = Available
        return equipmentInventoryRepository
                .findByEquipmentStatusIdAndDecommissionedFalse(1)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EquipmentInventoryResponse getEquipmentById(Integer id) {

        EquipmentInventory equipment =
                equipmentInventoryRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment not found with ID: " + id
                                )
                        );

        return mapToResponse(equipment);
    }

    @Override
    public EquipmentInventoryResponse getEquipmentBySerialNumber(
            String serialNumber
    ) {

        EquipmentInventory equipment =
                equipmentInventoryRepository
                        .findBySerialNumber(serialNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Equipment not found with serial number: "
                                                + serialNumber
                                )
                        );

        return mapToResponse(equipment);
    }

    @Override
    public List<EquipmentInventoryResponse> searchEquipment(
            String itemName
    ) {

        return equipmentInventoryRepository
                .findByItemNameContainingIgnoreCaseAndDecommissionedFalse(
                        itemName
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipmentInventoryResponse> getEquipmentByCategory(
            String category
    ) {

        return equipmentInventoryRepository
                .findByCategoryIgnoreCaseAndDecommissionedFalse(
                        category
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EquipmentInventoryResponse createEquipment(EquipmentInventoryRequest request) {

        Integer statusId = (request.getStatusId() != null) ? request.getStatusId() : 1;
        EquipmentStatus status = equipmentStatusRepository
                .findById(statusId)
                .or(() -> equipmentStatusRepository.findByStatus("Available"))
                .orElseGet(() -> equipmentStatusRepository.save(new EquipmentStatus(statusId, "Available")));

        String category = org.springframework.util.StringUtils.hasText(request.getCategory()) && !"-".equals(request.getCategory().trim())
                ? request.getCategory().trim()
                : "General";

        EquipmentInventory equipment = new EquipmentInventory();
        equipment.setSerialNumber(request.getSerialNumber() != null ? request.getSerialNumber().trim() : null);
        equipment.setItemName(request.getItemName() != null ? request.getItemName().trim() : null);
        equipment.setModel(request.getModel() != null ? request.getModel().trim() : null);
        equipment.setCategory(category);
        equipment.setNotes(request.getNotes());
        equipment.setEquipmentStatus(status);
        equipment.setDecommissioned(false);

        EquipmentInventory savedEquipment = equipmentInventoryRepository.save(equipment);
        return mapToResponse(savedEquipment);
    }

    @Override
    public EquipmentInventoryResponse updateEquipment(Integer id, EquipmentInventoryRequest request) {
        EquipmentInventory equipment = equipmentInventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with ID: " + id));

        String category = org.springframework.util.StringUtils.hasText(request.getCategory()) && !"-".equals(request.getCategory().trim())
                ? request.getCategory().trim()
                : "General";

        equipment.setSerialNumber(request.getSerialNumber() != null ? request.getSerialNumber().trim() : null);
        equipment.setItemName(request.getItemName() != null ? request.getItemName().trim() : null);
        equipment.setModel(request.getModel() != null ? request.getModel().trim() : null);
        equipment.setCategory(category);
        equipment.setNotes(request.getNotes());

        Integer statusId = (request.getStatusId() != null) ? request.getStatusId() : 1;
        EquipmentStatus status = equipmentStatusRepository
                .findById(statusId)
                .or(() -> equipmentStatusRepository.findByStatus("Available"))
                .orElseGet(() -> equipmentStatusRepository.save(new EquipmentStatus(statusId, "Available")));
        equipment.setEquipmentStatus(status);

        EquipmentInventory updatedEquipment = equipmentInventoryRepository.save(equipment);
        return mapToResponse(updatedEquipment);
    }

    @Override
    public void deleteEquipment(Integer id) {
        EquipmentInventory equipment = equipmentInventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found with ID: " + id));
        equipmentInventoryRepository.delete(equipment);
    }

    private EquipmentInventoryResponse mapToResponse(
            EquipmentInventory equipment
    ) {

        Integer statusId = null;
        String status = null;

        if (equipment.getEquipmentStatus() != null) {
            statusId = equipment.getEquipmentStatus().getId();
            status = equipment.getEquipmentStatus().getStatus();
        }

        return new EquipmentInventoryResponse(
                equipment.getId(),
                equipment.getSerialNumber(),
                equipment.getItemName(),
                equipment.getModel(),
                equipment.getCategory(),
                statusId,
                status,
                equipment.getNotes(),
                equipment.isDecommissioned()
        );
    }
}