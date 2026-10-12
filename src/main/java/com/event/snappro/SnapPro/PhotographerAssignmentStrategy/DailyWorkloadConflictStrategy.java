package com.event.snappro.SnapPro.PhotographerAssignmentStrategy;

import com.event.snappro.SnapPro.entity.EventBookingEntity;
import com.event.snappro.SnapPro.entity.PhotographerAssignmentEntity;
import com.event.snappro.SnapPro.entity.PhotographerProfile;
import com.event.snappro.SnapPro.repository.PhotographerAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DailyWorkloadConflictStrategy implements ConflictCheckStrategy {

    private final PhotographerAssignmentRepository assignmentRepository;

    @Value("${snappro.assignment.max-daily-hours:10}")
    private long maxDailyHours;

    @Override
    public Optional<String> check(PhotographerProfile photographer, EventBookingEntity booking) {
        long minutes = Duration.between(booking.getStartTime(), booking.getEndTime()).toMinutes();
        for (PhotographerAssignmentEntity a : assignmentRepository
                .findActiveByPhotographerAndDate(photographer.getId(), booking.getEventDate())) {
            EventBookingEntity other = a.getBooking();
            if (other.getId().equals(booking.getId())) continue;
            minutes += Duration.between(other.getStartTime(), other.getEndTime()).toMinutes();
        }
        if (minutes > maxDailyHours * 60) {
            return Optional.of("Daily workload would be " + (minutes / 60.0) + "h (limit " + maxDailyHours + "h)");
        }
        return Optional.empty();
    }
}

