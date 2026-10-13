package com.event.snappro.SnapPro.PhotographerAssignmentStrategy;

import com.event.snappro.SnapPro.entity.EventBookingEntity;
import com.event.snappro.SnapPro.entity.PhotographerAssignmentEntity;
import com.event.snappro.SnapPro.entity.PhotographerProfile;
import com.event.snappro.SnapPro.repository.PhotographerAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TimeOverlapConflictStrategy implements ConflictCheckStrategy {

    private final PhotographerAssignmentRepository assignmentRepository;

    @Override
    public Optional<String> check(PhotographerProfile photographer, EventBookingEntity booking) {
        for (PhotographerAssignmentEntity a : assignmentRepository
                .findActiveByPhotographerAndDate(photographer.getId(), booking.getEventDate())) {
            EventBookingEntity other = a.getBooking();
            if (other.getId().equals(booking.getId())) continue;
            boolean overlap = other.getStartTime().isBefore(booking.getEndTime())
                    && other.getEndTime().isAfter(booking.getStartTime());
            if (overlap) {
                return Optional.of("Time overlap with \"" + other.getEventTitle() + "\" ("
                        + other.getStartTime() + "-" + other.getEndTime() + ")");
            }
        }
        return Optional.empty();
    }
}

