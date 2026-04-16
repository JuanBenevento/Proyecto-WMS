package com.juanbenevento.wms.shared.domain.valueobject;

/**
 * Domain-wide constants for identifier prefixes and sentinel values.
 * These constants ensure consistent generation of business identifiers across bounded contexts.
 */
public final class WmsConstants {

    private WmsConstants() {
        // Utility class - prevent instantiation
    }

    /**
     * Prefix for License Plate Numbers (LPNs) generated during inbound receipt.
     * Format: LPN-{UUID_8_CHARS} e.g., LPN-A1B2C3D4
     */
    public static final String LPN_PREFIX = "LPN-";

    /**
     * Prefix for reserved stock items created during picking operations.
     * Format: PICK-{UUID_8_CHARS} e.g., PICK-X9Y8Z7W6
     */
    public static final String PICK_PREFIX = "PICK-";

    /**
     * Sentinel LPN value used when the actual LPN is unknown or not applicable.
     * Commonly used in shipping events where stock may come from multiple LPNs.
     */
    public static final String LPN_UNKNOWN = "LPN-UNKNOWN";

    /**
     * Sentinel value for LPN when multiple items are involved in a stock movement.
     */
    public static final String LPN_VARIOUS = "VARIOUS";

    /**
     * System tenant identifier used for background jobs and system operations.
     */
    public static final String SYSTEM_TENANT = "SYSTEM";

    /**
     * System user identifier used when no authenticated user is present.
     */
    public static final String SYSTEM_USER = "SYSTEM";
}
