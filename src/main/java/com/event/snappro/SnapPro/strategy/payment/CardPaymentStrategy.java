package com.event.snappro.SnapPro.strategy.payment;

import com.event.snappro.SnapPro.entity.EventBookingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete Strategy: Card Payment
 * Encapsulates the card payment algorithm. Simulates credit/debit card
 * gateway verification and generates an authorization reference.
 */
@Slf4j
@Component
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult processPayment(EventBookingEntity booking, double amount) {
        Integer bookingId = (booking != null) ? booking.getId() : null;
        String authCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String reference = "CARD-AUTH-" + authCode;

        log.info("[Strategy: CardPaymentStrategy] Successfully authorized card payment of LKR {} for booking #{}. Auth Ref: {}",
                amount, bookingId, reference);

        String message = String.format("Card payment authorized successfully for LKR %,.2f. Auth Ref: %s", amount, reference);
        return new PaymentResult(true, message, reference);
    }

    @Override
    public String getStrategyName() {
        return "Card";
    }
}
