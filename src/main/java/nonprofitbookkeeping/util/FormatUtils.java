package nonprofitbookkeeping.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for formatting currency values across the UI. The format
 * pattern can be updated at runtime, typically loaded from user preferences.
 */
public final class FormatUtils {
    /** Default currency pattern used when none has been customised. */
    private static final String DEFAULT_PATTERN = "$#,##0.00";

    /**
     * Tracks the active pattern so that callers can retrieve or override it. The
     * field is {@code volatile} because it can be mutated from different UI
     * threads (Swing and JavaFX) in the application.
     */
    private static volatile String pattern = DEFAULT_PATTERN;

    /**
     * Thread-safe container for {@link DecimalFormat} instances. {@link
     * DecimalFormat} is mutable and not thread-safe, so a {@link ThreadLocal}
     * keeps per-thread formatters while still honouring runtime changes to the
     * pattern.
     */
    private static final ThreadLocal<DecimalFormat> FORMATTER =
            ThreadLocal.withInitial(FormatUtils::createFormatter);

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
        return FORMATTER.get().format(value);
    }

    /**
     * Updates the currency format pattern. If the supplied pattern is null or
     * empty, the existing format is retained.
     *
     * @param newPattern DecimalFormat pattern string
     */
    public static void setCurrencyFormat(String newPattern) {
        if (newPattern == null || newPattern.isBlank()) {
            return;
        }

        pattern = newPattern;
        FORMATTER.set(createFormatter(pattern));
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
     * Creates a {@link DecimalFormat} for the supplied pattern using default
     * currency symbols for the active locale.
     *
     * @param patternToApply pattern describing how currency should be rendered
     * @return configured {@link DecimalFormat}
     */
    public static DecimalFormat createFormatter(String patternToApply) {
        String resolvedPattern = (patternToApply == null || patternToApply.isBlank())
                ? DEFAULT_PATTERN
                : patternToApply;

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.getDefault());
        DecimalFormat decimalFormat = new DecimalFormat(resolvedPattern, symbols);
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    }

    /**
     * Convenience overload that uses the currently configured pattern.
     *
     * @return formatter bound to the active currency pattern
     */
    public static DecimalFormat createFormatter() {
        return createFormatter(pattern);
    }

    /**
     * Resets the cached formatter so that subsequent calls honour the latest
     * locale defaults. Primarily intended for tests.
     */
    static void resetFormatterCache() {
        FORMATTER.remove();
    }
}
