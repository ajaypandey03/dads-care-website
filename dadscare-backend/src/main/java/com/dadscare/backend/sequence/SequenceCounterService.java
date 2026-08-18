package com.dadscare.backend.sequence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates unique, sequential, org-scoped reference codes — e.g. "DC-000482" — for
 * every lock open/close alert sent over WhatsApp (see Dad's Care Platform Design in
 * Confluence, "Sequential WhatsApp reference codes").
 *
 * <p>Uses MySQL's {@code INSERT ... ON DUPLICATE KEY UPDATE ... LAST_INSERT_ID(expr)}
 * idiom to atomically read-and-increment the counter in a single round trip, under an
 * implicit row lock — this is race-safe under concurrent callers without a separate
 * {@code SELECT ... FOR UPDATE}, and is the standard way to implement a MySQL-backed
 * sequence. {@code REQUIRES_NEW} so a caller's own longer-running transaction (e.g. the
 * one creating the Alert) never holds this row lock longer than the single increment,
 * and a rollback of the outer transaction does not "give back" a consumed number —
 * exactly like a database SEQUENCE or an invoice numbering scheme, gaps are acceptable,
 * duplicates are not.
 */
@Service
@RequiredArgsConstructor
public class SequenceCounterService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Atomically returns the next value (starting at 1) for the given org + counter type,
     * creating the counter row on first use.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long nextValue(long organizationId, String counterType) {
        jdbcTemplate.update(
                """
                INSERT INTO sequence_counters (organization_id, counter_type, next_value)
                VALUES (?, ?, 1)
                ON DUPLICATE KEY UPDATE next_value = LAST_INSERT_ID(next_value + 1)
                """,
                organizationId,
                counterType);
        Long value = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (value == null) {
            throw new IllegalStateException("LAST_INSERT_ID() returned null — unexpected");
        }
        return value;
    }

    /** Formats a raw counter value as the customer-facing reference code, e.g. "DC-000482". */
    public String formatReferenceCode(String orgCodePrefix, long value) {
        return "%s-%06d".formatted(orgCodePrefix, value);
    }

    /** Convenience: generate and format in one call for the standard "alert" counter type. */
    public String nextAlertReferenceCode(long organizationId, String orgCodePrefix) {
        return formatReferenceCode(orgCodePrefix, nextValue(organizationId, CounterTypes.ALERT));
    }

    public static final class CounterTypes {
        public static final String ALERT = "ALERT";

        private CounterTypes() {
        }
    }
}
