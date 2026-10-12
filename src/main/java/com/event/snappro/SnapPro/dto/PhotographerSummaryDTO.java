package com.event.snappro.SnapPro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotographerSummaryDTO {
    private Integer photographerId;
    private String name;
    private String email;
    private String mobile;
    private Integer experienceYears;
    private String bio;
    private boolean active;          // user account status is ACTIVE
    private long upcomingShifts;     // active assignments from today onwards
}