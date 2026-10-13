
package com.event.snappro.SnapPro.repository;

import com.event.snappro.SnapPro.entity.CustomerFeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerFeedbackRepository
        extends JpaRepository<CustomerFeedbackEntity, Integer> {

    // Get only non-deleted feedback
    List<CustomerFeedbackEntity>
    findByIsDeletedFalseOrderByCreatedAtDesc();

    // Filter non-deleted feedback by status
    List<CustomerFeedbackEntity>
    findByFeedbackStatusIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Integer feedbackStatusId
    );

    // Get non-deleted escalated complaints
    List<CustomerFeedbackEntity>
    findByEscalatedToManagerTrueAndIsDeletedFalseOrderByCreatedAtDesc();

    // Get non-deleted feedback by customer
    List<CustomerFeedbackEntity>
    findByCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Integer customerId
    );

    // Find a non-deleted feedback by booking
    List<CustomerFeedbackEntity>
    findByBookingIdAndIsDeletedFalse(Integer bookingId);

    // Find any feedback by booking (regardless of deleted status, to respect booking_id_UNIQUE constraint)
    Optional<CustomerFeedbackEntity> findByBookingId(Integer bookingId);

    // Find a specific non-deleted feedback
    Optional<CustomerFeedbackEntity>
    findByIdAndIsDeletedFalse(Integer id);
}
