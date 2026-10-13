package com.event.snappro.SnapPro.dto;

public class EquipmentDashboardResponse {

    private long availableItems;

    private long checkedOutItems;

    private long expectedReturnsToday;

    private long maintenanceItems;

    public EquipmentDashboardResponse() {
    }

    public EquipmentDashboardResponse(
            long availableItems,
            long checkedOutItems,
            long expectedReturnsToday,
            long maintenanceItems
    ) {
        this.availableItems = availableItems;
        this.checkedOutItems = checkedOutItems;
        this.expectedReturnsToday = expectedReturnsToday;
        this.maintenanceItems = maintenanceItems;
    }

    public long getAvailableItems() {
        return availableItems;
    }

    public void setAvailableItems(long availableItems) {
        this.availableItems = availableItems;
    }

    public long getCheckedOutItems() {
        return checkedOutItems;
    }

    public void setCheckedOutItems(long checkedOutItems) {
        this.checkedOutItems = checkedOutItems;
    }

    public long getExpectedReturnsToday() {
        return expectedReturnsToday;
    }

    public void setExpectedReturnsToday(long expectedReturnsToday) {
        this.expectedReturnsToday = expectedReturnsToday;
    }

    public long getMaintenanceItems() {
        return maintenanceItems;
    }

    public void setMaintenanceItems(long maintenanceItems) {
        this.maintenanceItems = maintenanceItems;
    }

    public long getAvailable() {
        return availableItems;
    }

    public long getCheckedOut() {
        return checkedOutItems;
    }

    public long getExpectedReturns() {
        return expectedReturnsToday;
    }

    public long getMaintenance() {
        return maintenanceItems;
    }

    public long getAvailableCount() {
        return availableItems;
    }

    public long getCheckedOutCount() {
        return checkedOutItems;
    }

    public long getExpectedReturnsCount() {
        return expectedReturnsToday;
    }

    public long getMaintenanceCount() {
        return maintenanceItems;
    }
}