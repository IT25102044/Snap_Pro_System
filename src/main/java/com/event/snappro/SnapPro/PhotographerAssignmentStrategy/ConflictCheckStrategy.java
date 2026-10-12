package com.event.snappro.SnapPro.PhotographerAssignmentStrategy;

import com.event.snappro.SnapPro.entity.EventBookingEntity;
import com.event.snappro.SnapPro.entity.PhotographerProfile;

import java.util.Optional;

/**
 * STRATEGY PATTERN: each scheduling rule is its own interchangeable strategy.
 * Returns a conflict message, or empty when the photographer passes the rule.
 */
public interface ConflictCheckStrategy {
    Optional<String> check(PhotographerProfile photographer, EventBookingEntity booking);
}

