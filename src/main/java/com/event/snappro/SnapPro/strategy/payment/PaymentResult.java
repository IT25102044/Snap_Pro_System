package com.event.snappro.SnapPro.strategy.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates the result of a payment operation executed by a PaymentStrategy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private boolean success;
    private String message;
    private String transactionReference;
}
