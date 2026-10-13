package com.event.snappro.SnapPro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponseDTO {
    private Integer assignmentId;
    private Integer bookingId;
    private String eventTitle;
    private String eventLocation;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer photographerId;
    private String photographerName;
    private String assignedByName;
    private LocalDateTime assignedAt;
    private boolean active;
    private String reassignmentReason;
}

