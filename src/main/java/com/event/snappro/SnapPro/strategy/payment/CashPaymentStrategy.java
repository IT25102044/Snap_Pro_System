package com.event.snappro.SnapPro.strategy.payment;

import com.event.snappro.SnapPro.entity.EventBookingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy: Cash Payment
 * Encapsulates the cash payment algorithm. In this workflow, payments
 * are registered and scheduled for in-person settlement on the event date.
 */
@Slf4j
@Component
public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(EventBookingEntity booking, double amount) {
        Integer bookingId = (booking != null) ? booking.getId() : null;
        String reference = "CASH-BK-" + (bookingId != null ? bookingId : System.currentTimeMillis());

        log.info("[Strategy: CashPaymentStrategy] Processing cash settlement for booking #{}. Amount: LKR {}. Scheduled for collection on event date.",
                bookingId, amount);

        String message = String.format("Cash payment registered. Total of LKR %,.2f to be collected on the event date.", amount);
        return new PaymentResult(true, message, reference);
    }

    @Override
    public String getStrategyName() {
        return "Cash";
    }
}
