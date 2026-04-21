package com.juanbenevento.wms.inventory.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LotStatus enumeration.
 */
@DisplayName("LotStatus")
class LotStatusTest {

    @ParameterizedTest
    @DisplayName("canIssue() should return true only for ACTIVE status")
    @EnumSource(LotStatus.class)
    void testCanIssue_allStatuses(LotStatus status) {
        boolean result = status.canIssue();

        if (status == LotStatus.ACTIVE) {
            assertTrue(result, "ACTIVE lot should be issuable");
        } else {
            assertFalse(result, status + " lot should NOT be issuable");
        }
    }

    @Test
    @DisplayName("ACTIVE canIssue should return true")
    void testCanIssue_activeTrue() {
        assertTrue(LotStatus.ACTIVE.canIssue());
    }

    @Test
    @DisplayName("EXHAUSTED canIssue should return false")
    void testCanIssue_exhaustedFalse() {
        assertFalse(LotStatus.EXHAUSTED.canIssue());
    }

    @Test
    @DisplayName("EXPIRED canIssue should return false")
    void testCanIssue_expiredFalse() {
        assertFalse(LotStatus.EXPIRED.canIssue());
    }

    @Test
    @DisplayName("QUARANTINE canIssue should return false")
    void testCanIssue_quarantineFalse() {
        assertFalse(LotStatus.QUARANTINE.canIssue());
    }
}