package com.event.snappro.SnapPro.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignableEventDTO {
    private Integer bookingId;
    private String eventTitle;
    private String eventLocation;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String customerName;
    private int assignedCount;
    private List<String> assignedPhotographers;
}

