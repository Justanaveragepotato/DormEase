public final class ValidationUtils {

    // Meaningful array used for room-type validation.
    private static final String[] VALID_ROOM_TYPES = {
        "Single",
        "Double",
        "Shared"
    };

    private ValidationUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean containsDelimiter(String value) {
        return value != null && value.contains("|");
    }

    public static boolean isValidField(String value) {
        return !isBlank(value) && !containsDelimiter(value);
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static Integer parseInt(String value) {
        try {
            return Integer.valueOf(trimToNull(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }
        if (value.trim().equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (value.trim().equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        return null;
    }

    public static String normalizeRoomType(String roomType) {
        String normalized = trimToNull(roomType);
        if (normalized == null) {
            return null;
        }

        for (String validType : VALID_ROOM_TYPES) {
            if (validType.equalsIgnoreCase(normalized)) {
                return validType;
            }
        }

        return null;
    }

    public static boolean isValidRoomType(String roomType) {
        return normalizeRoomType(roomType) != null;
    }
}
