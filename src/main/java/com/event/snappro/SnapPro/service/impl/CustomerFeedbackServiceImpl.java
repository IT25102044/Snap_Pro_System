
package com.event.snappro.SnapPro.service.impl;

import com.event.snappro.SnapPro.dto.FeedbackCreateDTO;
import com.event.snappro.SnapPro.entity.CustomerFeedbackEntity;
import com.event.snappro.SnapPro.repository.CustomerFeedbackRepository;
import com.event.snappro.SnapPro.repository.FeedbackStatusRepository;
import com.event.snappro.SnapPro.service.CustomerFeedbackService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerFeedbackServiceImpl implements CustomerFeedbackService {

    private final CustomerFeedbackRepository customerFeedbackRepository;
    private final FeedbackStatusRepository feedbackStatusRepository;

    public CustomerFeedbackServiceImpl(
            CustomerFeedbackRepository customerFeedbackRepository,
            FeedbackStatusRepository feedbackStatusRepository
    ) {
        this.customerFeedbackRepository = customerFeedbackRepository;
        this.feedbackStatusRepository = feedbackStatusRepository;
    }

    @Override
    public CustomerFeedbackEntity createFeedback(FeedbackCreateDTO dto) {
        // Check if a feedback already exists for this booking ID (including soft-deleted ones due to UNIQUE constraint)
        Optional<CustomerFeedbackEntity> existingOpt = customerFeedbackRepository.findByBookingId(dto.getBookingId());
        CustomerFeedbackEntity feedback;

        if (existingOpt.isPresent()) {
            feedback = existingOpt.get();
            feedback.setCustomerId(dto.getCustomerId());
            feedback.setIsDeleted(false);
            feedback.setEscalatedToManager(false);
            feedback.setEscalationRemarks(null);
            feedback.setCroResponse(null);
            feedback.setHandledBy(null);
        } else {
            feedback = new CustomerFeedbackEntity();
            feedback.setCustomerId(dto.getCustomerId());
            feedback.setBookingId(dto.getBookingId());
            feedback.setIsDeleted(false);
            feedback.setEscalatedToManager(false);
        }

        feedback.setRating(dto.getRating());
        feedback.setReviewText(dto.getReviewText());
        feedback.setFeedbackStatusId(1); // Pending

        return customerFeedbackRepository.save(feedback);
    }

    @Override
    public List<CustomerFeedbackEntity> getAllFeedback() {
        return customerFeedbackRepository
                .findByIsDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    public CustomerFeedbackEntity getFeedbackById(Integer id) {
        return customerFeedbackRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Feedback not found with ID: " + id
                        )
                );
    }

    @Override
    public CustomerFeedbackEntity updateFeedback(
            Integer id,
            String croResponse,
            Integer feedbackStatusId,
            Integer handledBy
    ) {
        CustomerFeedbackEntity feedback = getFeedbackById(id);

        if (croResponse != null) {
            feedback.setCroResponse(croResponse);
        }

        if (feedbackStatusId != null) {
            feedbackStatusRepository.findById(feedbackStatusId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Feedback status not found with ID: "
                                            + feedbackStatusId
                            )
                    );

            feedback.setFeedbackStatusId(feedbackStatusId);
        }

        if (handledBy != null) {
            feedback.setHandledBy(handledBy);
        }

        return customerFeedbackRepository.save(feedback);
    }

    @Override
    public CustomerFeedbackEntity escalateFeedback(
            Integer id,
            String escalationRemarks,
            Integer handledBy
    ) {
        CustomerFeedbackEntity feedback = getFeedbackById(id);

        feedback.setEscalatedToManager(true);
        feedback.setEscalationRemarks(escalationRemarks);

        if (handledBy != null) {
            feedback.setHandledBy(handledBy);
        }

        return customerFeedbackRepository.save(feedback);
    }

    @Override
    public List<CustomerFeedbackEntity> getFeedbackByStatus(
            Integer feedbackStatusId
    ) {
        return customerFeedbackRepository
                .findByFeedbackStatusIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        feedbackStatusId
                );
    }

    @Override
    public List<CustomerFeedbackEntity> getEscalatedFeedback() {
        return customerFeedbackRepository
                .findByEscalatedToManagerTrueAndIsDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    public List<CustomerFeedbackEntity> getFeedbackByCustomer(
            Integer customerId
    ) {
        return customerFeedbackRepository
                .findByCustomerIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        customerId
                );
    }

    @Override
    public List<CustomerFeedbackEntity> getFeedbackByBooking(
            Integer bookingId
    ) {
        return customerFeedbackRepository
                .findByBookingIdAndIsDeletedFalse(bookingId);
    }

    @Override
    public void softDeleteFeedback(Integer id) {
        CustomerFeedbackEntity feedback = getFeedbackById(id);

        feedback.setIsDeleted(true);

        customerFeedbackRepository.save(feedback);
    }
}
