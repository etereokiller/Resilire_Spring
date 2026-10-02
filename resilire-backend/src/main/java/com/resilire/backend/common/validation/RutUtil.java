package com.resilire.backend.common.validation;

/**
 * Chilean RUT (Rol Unico Tributario) normalization and check-digit validation.
 * Format: 7-8 body digits, a hyphen, and a check digit (0-9 or K) computed via modulo 11.
 */
public final class RutUtil {

    private RutUtil() {
    }

    /**
     * Strips dots/spaces and uppercases the check digit, e.g. " 12.345.678-k" -> "12345678-K".
     * Leaves the value unchanged (for later validation to reject) if it has no hyphen.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replace(".", "").replace(" ", "").toUpperCase();
        return cleaned;
    }

    public static boolean isValid(String rut) {
        if (rut == null) {
            return false;
        }
        String normalized = normalize(rut);
        if (!normalized.matches("\\d{7,8}-[0-9K]")) {
            return false;
        }
        String body = normalized.substring(0, normalized.indexOf('-'));
        char checkDigit = normalized.charAt(normalized.length() - 1);
        return computeCheckDigit(body) == checkDigit;
    }

    private static char computeCheckDigit(String body) {
        int sum = 0;
        int multiplier = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += (body.charAt(i) - '0') * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }
        int remainder = 11 - (sum % 11);
        if (remainder == 11) {
            return '0';
        }
        if (remainder == 10) {
            return 'K';
        }
        return (char) ('0' + remainder);
    }
}
