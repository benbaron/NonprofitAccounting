package nonprofitbookkeeping.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for formatting currency values across the UI. The format
 * pattern can be updated at runtime, typically loaded from user preferences.
 */
public final class FormatUtils {
    /** Default currency pattern used when nothing has been configured. */
    public static final String DEFAULT_PATTERN = "$#,##0.00";

    /** The active pattern for formatting monetary values. */
    private static volatile String pattern = DEFAULT_PATTERN;

    /** The locale used for currency formatting. Defaults to the JVM format locale. */
    private static volatile Locale locale = Locale.getDefault(Locale.Category.FORMAT);

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
        String effective = normalisePattern(newPattern);

        if (!Objects.equals(effective, pattern)) {
            // Validate before committing to avoid leaving the utility in a bad state.
            buildFormatter(effective, locale); // validate pattern before committing
            pattern = effective;
        }
    }

    /**
     * Sets the locale used for currency formatting.
     *
     * @param newLocale the locale to use. When {@code null} the system default format locale
     *                  is applied instead.
     */
    public static void setCurrencyLocale(Locale newLocale) {
        Locale effective = (newLocale == null)
                ? Locale.getDefault(Locale.Category.FORMAT)
                : newLocale;

        if (!effective.equals(locale)) {
            locale = effective;
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
     * Provides a snapshot of the locale currently used for currency formatting.
     *
     * @return the active locale
     */
    public static Locale getCurrencyLocale() {
        return locale;
    }

    /**
     * Creates a {@link DecimalFormat} using the currently configured pattern and locale.
     *
     * @return a new formatter instance configured with the current state
     */
    public static DecimalFormat createFormatter() {
        return buildFormatter(pattern, locale);
    }

    /**
     * Creates a {@link DecimalFormat} using the supplied pattern and the current locale.
     * If {@code suppliedPattern} is {@code null} or blank the configured pattern is used instead.
     *
     * @param suppliedPattern pattern to apply, if non-empty
     * @return formatter instance using the derived pattern and locale
     */
    public static DecimalFormat createFormatter(String suppliedPattern) {
        String effectivePattern = normalisePattern(suppliedPattern);
        return buildFormatter(effectivePattern, locale);
    }

    private static String normalisePattern(String candidate) {
        return (candidate == null || candidate.trim().isEmpty()) ? pattern : candidate;
    }

    private static DecimalFormat buildFormatter(String patternValue, Locale localeValue) {
        Locale activeLocale = (localeValue == null)
                ? Locale.getDefault(Locale.Category.FORMAT)
                : localeValue;
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(activeLocale);
        DecimalFormat decimalFormat = new DecimalFormat(patternValue, symbols);
        decimalFormat.setParseBigDecimal(true);
        return decimalFormat;
    }
}
