package com.event.snappro.SnapPro.event;

import java.time.LocalDate;
import java.time.LocalTime;

public record AssignmentChangedEvent(
        Type type,
        Integer assignmentId,
        Integer bookingId,
        String eventTitle,
        LocalDate eventDate,
        LocalTime startTime,
        LocalTime endTime,
        String photographerName,
        String photographerEmail,
        String reason) {

    public enum Type { ASSIGNED, REASSIGNED, REMOVED }
}

