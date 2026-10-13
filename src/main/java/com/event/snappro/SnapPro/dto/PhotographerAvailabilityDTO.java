package com.event.snappro.SnapPro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotographerAvailabilityDTO {
    private Integer photographerId;
    private String name;
    private String email;
    private Integer experienceYears;
    private String bio;
    private boolean available;
    private List<String> conflicts;
    private boolean previouslyAssigned;   // was replaced/removed from this event before
}

