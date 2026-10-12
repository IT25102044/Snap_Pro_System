package com.event.snappro.SnapPro.repository;

import com.event.snappro.SnapPro.entity.EventBookingEntity;
import com.event.snappro.SnapPro.entity.PhotographerAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PhotographerAssignmentRepository extends JpaRepository<PhotographerAssignmentEntity, Integer> {

    List<PhotographerAssignmentEntity> findByBooking_IdAndActiveTrue(Integer bookingId);

    List<PhotographerAssignmentEntity> findByActiveTrueOrderByBooking_EventDateAscBooking_StartTimeAsc();

    List<PhotographerAssignmentEntity> findByPhotographer_IdAndActiveTrueOrderByBooking_EventDateAscBooking_StartTimeAsc(Integer photographerId);

    boolean existsByBooking_IdAndPhotographer_IdAndActiveTrue(Integer bookingId, Integer photographerId);

    // true if this photographer was on this event before and was replaced/removed
    boolean existsByBooking_IdAndPhotographer_IdAndActiveFalse(Integer bookingId, Integer photographerId);

    @Query("SELECT a FROM PhotographerAssignmentEntity a JOIN FETCH a.booking b " +
            "WHERE a.photographer.id = :photographerId AND a.active = true AND b.eventDate = :date")
    List<PhotographerAssignmentEntity> findActiveByPhotographerAndDate(@Param("photographerId") Integer photographerId,
                                                                       @Param("date") LocalDate date);

    @Query("SELECT b FROM EventBookingEntity b WHERE LOWER(b.bookingStatus.bookingStatus) = 'confirmed' " +
            "AND b.eventDate >= :today ORDER BY b.eventDate ASC, b.startTime ASC")
    List<EventBookingEntity> findAssignableBookings(@Param("today") LocalDate today);
}

