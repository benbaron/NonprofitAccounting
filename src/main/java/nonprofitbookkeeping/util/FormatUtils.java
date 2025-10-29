package nonprofitbookkeeping.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Utility class for formatting currency values across the UI. The format
 * pattern can be updated at runtime, typically loaded from user preferences.
 */
public final class FormatUtils {
    /** Default currency pattern. */
    private static final String DEFAULT_PATTERN = "$#,##0.00";
    /** Guard for updating global formatter state. */
    private static final Object FORMAT_LOCK = new Object();
    /** Active pattern used for new {@link DecimalFormat} instances. */
    private static volatile String pattern = DEFAULT_PATTERN;
    /** Thread-local formatter to avoid sharing {@link DecimalFormat} instances. */
    private static ThreadLocal<DecimalFormat> formatter =
            ThreadLocal.withInitial(() -> createFormatter(DEFAULT_PATTERN));

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
        return formatter.get().format(value);
    }

    /**
     * Updates the currency format pattern. If the supplied pattern is null or
     * empty, the default format is used.
     *
     * @param newPattern DecimalFormat pattern string
     */
    public static void setCurrencyFormat(String newPattern) {
        synchronized (FORMAT_LOCK) {
            String updated = (newPattern == null || newPattern.isBlank()) ? DEFAULT_PATTERN : newPattern;
            if (!Objects.equals(pattern, updated)) {
                pattern = updated;
                formatter = ThreadLocal.withInitial(() -> createFormatter(pattern));
            } else {
                pattern = updated;
            }
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

    /**
     * Creates a {@link DecimalFormat} instance using the active pattern.
     *
     * @return a formatter configured with the current pattern
     */
    public static DecimalFormat createFormatter() {
        return createFormatter(pattern);
    }

    /**
     * Creates a {@link DecimalFormat} instance using the supplied pattern.
     *
     * @param customPattern custom pattern to apply; if blank the default pattern is used
     * @return configured {@link DecimalFormat}
     */
    public static DecimalFormat createFormatter(String customPattern) {
        String effectivePattern = (customPattern == null || customPattern.isBlank())
                ? DEFAULT_PATTERN
                : customPattern;
        DecimalFormat decimalFormat = new DecimalFormat(effectivePattern);
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    }
}
