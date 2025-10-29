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
    private static DecimalFormat formatter = new DecimalFormat(pattern);

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
        return formatter.format(value);
    }

    /**
     * Updates the currency format pattern. If the supplied pattern is null or
     * empty, the existing format is retained.
     *
     * @param newPattern DecimalFormat pattern string
     */
    public static void setCurrencyFormat(String newPattern) {
        if (newPattern != null && !newPattern.isEmpty()) {
            pattern = newPattern;
            formatter = new DecimalFormat(pattern);
        }
    }

    /**
     * Retrieves the current currency format pattern.
     *
     * @return active pattern string
     */
    public static String getCurrencyFormat() {
        return pattern;
    }
}
