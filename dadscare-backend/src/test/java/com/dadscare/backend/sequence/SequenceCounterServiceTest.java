package com.dadscare.backend.sequence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class SequenceCounterServiceTest {

    // formatReferenceCode is pure logic — deliberately tested without a Spring context
    // or a real database. The atomic increment itself (nextValue) relies on a
    // MySQL-specific SQL idiom and needs to be exercised against real MySQL (e.g. in a
    // docker-compose-backed integration test), not H2 — see class javadoc on
    // SequenceCounterService.
    private final SequenceCounterService service = new SequenceCounterService(new JdbcTemplate());

    @Test
    void formatsReferenceCodeWithSixDigitZeroPadding() {
        assertThat(service.formatReferenceCode("DC", 482)).isEqualTo("DC-000482");
    }

    @Test
    void formatsFirstValueAsOne() {
        assertThat(service.formatReferenceCode("DC", 1)).isEqualTo("DC-000001");
    }

    @Test
    void doesNotTruncateBeyondSixDigits() {
        assertThat(service.formatReferenceCode("DC", 1_234_567)).isEqualTo("DC-1234567");
    }
}
