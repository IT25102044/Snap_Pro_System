package com.event.snappro.SnapPro.strategy.payment;

import com.event.snappro.SnapPro.entity.EventBookingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Context class in the Strategy Pattern.
 * Maintains a reference to a PaymentStrategy and delegates execution to it.
 * This decouples the client from concrete payment algorithms and avoids
 * large if-else or switch statements when choosing between payment behaviors.
 */
@Slf4j
@Component
public class PaymentContext {

    // Current interchangeable strategy reference
    private PaymentStrategy strategy;

    // Registry of available strategies (e.g. CASH -> CashPaymentStrategy, CARD -> CardPaymentStrategy)
    private final Map<String, PaymentStrategy> strategyRegistry;

    public PaymentContext(List<PaymentStrategy> strategies) {
        this.strategyRegistry = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getStrategyName().toUpperCase(),
                        s -> s
                ));
    }

    /**
     * Classic GoF setter: Allows changing the strategy algorithm at runtime.
     *
     * @param strategy the concrete PaymentStrategy to use
     */
    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Helper method to choose and switch the strategy based on the database payment method name.
     * Maps names like "Card", "Credit Card", "Card Payment" to CardPaymentStrategy,
     * and defaults to CashPaymentStrategy ("Cash", "Cash on Event Date").
     *
     * @param methodName the name of the payment method from the database
     */
    public void setStrategyByMethodName(String methodName) {
        if (methodName != null && methodName.trim().toUpperCase().contains("CARD")) {
            this.strategy = strategyRegistry.get("CARD");
        } else {
            this.strategy = strategyRegistry.get("CASH");
        }
    }

    /**
     * Executes the payment algorithm using the currently configured strategy.
     *
     * @param booking the booking entity
     * @param amount  the amount to process
     * @return the result of the payment processing
     */
    public PaymentResult executePayment(EventBookingEntity booking, double amount) {
        if (this.strategy == null) {
            setStrategyByMethodName("Cash");
        }

        log.info("[PaymentContext] Delegating execution to strategy: {}", this.strategy.getStrategyName());
        return this.strategy.processPayment(booking, amount);
    }

    public PaymentStrategy getStrategy() {
        return this.strategy;
    }
}
