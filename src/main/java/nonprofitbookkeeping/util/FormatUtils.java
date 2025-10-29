package nonprofitbookkeeping.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Utility class for formatting currency values across the UI. The format
 * pattern can be updated at runtime, typically loaded from user preferences.
 */
public final class FormatUtils {
    /** Default currency pattern. */
    private static String pattern = "$#,##0.00";
    /** Cached formatter instance used as the template for clones. */
    private static DecimalFormat formatter = new DecimalFormat(pattern);
    /** Synchronization guard for formatter updates. */
    private static final Object LOCK = new Object();

    private FormatUtils() {}

    /**
     * Formats the given value using the current currency pattern.
     *
     * @param value value to format
     * @return formatted currency string or empty string if {@code value} is null
     */
    public static String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return createFormatter().format(value);
    }

    /**
     * Updates the currency format pattern. If the supplied pattern is null or
     * empty, the existing format is retained.
     *
     * @param newPattern DecimalFormat pattern string
     */
    public static void setCurrencyFormat(String newPattern) {
        if (newPattern != null && !newPattern.isEmpty()) {
            synchronized (LOCK) {
                pattern = newPattern;
                formatter = new DecimalFormat(pattern);
            }
        }
    }

    /**
     * Creates a {@link DecimalFormat} instance using either the supplied pattern or,
     * if the argument is {@code null} or blank, the currently configured pattern.
     * The returned instance is a clone of the cached formatter which avoids
     * concurrency issues with {@link DecimalFormat}'s mutable state.
     *
     * @param patternOverride optional pattern to apply
     * @return a formatter configured with the resolved pattern
     */
    public static DecimalFormat createFormatter(String patternOverride) {
        synchronized (LOCK) {
            if (patternOverride != null && !patternOverride.isBlank()) {
                return new DecimalFormat(patternOverride);
            }
            return (DecimalFormat) formatter.clone();
        }
    }

    /**
     * Creates a {@link DecimalFormat} using the currently configured pattern.
     *
     * @return formatter configured with the active currency pattern
     */
    public static DecimalFormat createFormatter() {
        return createFormatter(null);
    }

    /**
     * Retrieves the current currency format pattern.
     *
     * @return active pattern string
     */
    public static String getCurrencyFormat() {
        synchronized (LOCK) {
            return pattern;
        }
    }
}
