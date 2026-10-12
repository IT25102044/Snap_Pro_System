package com.event.snappro.SnapPro.strategy.payment;

import com.event.snappro.SnapPro.entity.EventBookingEntity;

/**
 * Strategy Interface for Payment Processing.
 * Defines the contract for all interchangeable payment algorithms.
 */
public interface PaymentStrategy {

    /**
     * Executes the payment algorithm for the given booking and amount.
     *
     * @param booking the event booking entity
     * @param amount  the amount to process
     * @return PaymentResult indicating success status and transaction details
     */
    PaymentResult processPayment(EventBookingEntity booking, double amount);

    /**
     * Identifies the strategy name (e.g., "Cash" or "Card").
     */
    String getStrategyName();
}
