package com.event.snappro.SnapPro.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * OBSERVER PATTERN: the service publishes an event; this listener reacts after the DB commit.
 */
@Slf4j
@Component
public class AssignmentNotificationListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAssignmentChanged(AssignmentChangedEvent e) {
        log.info("[NOTIFY] {} -> {} <{}> | event '{}' on {} {}-{} {}",
                e.type(), e.photographerName(), e.photographerEmail(),
                e.eventTitle(), e.eventDate(), e.startTime(), e.endTime(),
                e.reason() == null ? "" : "| reason: " + e.reason());
    }
}

