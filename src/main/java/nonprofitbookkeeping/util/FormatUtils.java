package nonprofitbookkeeping.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;

/**
 * Utility class for formatting currency values across the UI. The format
 * pattern can be updated at runtime, typically loaded from user preferences.
 */
public final class FormatUtils {
    /** Default currency pattern. */
    private static String pattern = "$#,##0.00";
    private static final Object FORMAT_LOCK = new Object();
    private static DecimalFormat formatter = createFormatter(pattern);

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
        synchronized (FORMAT_LOCK) {
            return formatter.format(value);
        }
    }

    /**
     * Updates the currency format pattern. If the supplied pattern is null or
     * empty, the existing format is retained.
     *
     * @param newPattern DecimalFormat pattern string
     */
    public static void setCurrencyFormat(String newPattern) {
        if (newPattern != null && !newPattern.isEmpty()) {
            synchronized (FORMAT_LOCK) {
                pattern = newPattern;
                formatter = createFormatter(pattern);
            }
        }
    }

    /**
     * Retrieves the current currency format pattern.
     *
     * @return active pattern string
     */
    public static String getCurrencyFormat() {
        synchronized (FORMAT_LOCK) {
            return pattern;
        }
    }

    /**
     * Parses the provided text using the active currency pattern, falling back to
     * a locale-aware sanitisation when the exact pattern cannot be matched.
     *
     * @param text currency text entered by the user
     * @return parsed {@link BigDecimal} or {@code null} when the text cannot be parsed
     */
    public static BigDecimal parseCurrency(String text) {
        if (text == null) {
            return null;
        }

        String normalized = text.replace('\u00A0', ' ').trim();
        if (normalized.isEmpty()) {
            return null;
        }

        boolean negative = false;
        if (normalized.startsWith("(") && normalized.endsWith(")")) {
            negative = true;
            normalized = normalized.substring(1, normalized.length() - 1).trim();
            if (normalized.startsWith("-")) {
                normalized = normalized.substring(1).trim();
            }
            if (normalized.isEmpty()) {
                return null;
            }
        }

        BigDecimal parsed = parseWithFormatter(normalized);
        if (parsed == null) {
            DecimalFormatSymbols symbols;
            synchronized (FORMAT_LOCK) {
                symbols = formatter.getDecimalFormatSymbols();
            }
            parsed = parseLoosely(normalized, symbols);
        }

        if (parsed == null) {
            return null;
        }

        return negative ? parsed.negate() : parsed;
    }

    private static DecimalFormat createFormatter(String pattern) {
        DecimalFormat format = new DecimalFormat(pattern);
        format.setParseBigDecimal(true);
        return format;
    }

    private static BigDecimal parseWithFormatter(String text) {
        synchronized (FORMAT_LOCK) {
            ParsePosition position = new ParsePosition(0);
            Number number = formatter.parse(text, position);
            if (number == null || position.getIndex() != text.length()) {
                return null;
            }
            if (number instanceof BigDecimal bigDecimal) {
                return bigDecimal;
            }
            return new BigDecimal(number.toString());
        }
    }

    private static BigDecimal parseLoosely(String text, DecimalFormatSymbols symbols) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        char grouping = symbols.getGroupingSeparator();
        char decimal = symbols.getDecimalSeparator();
        char minus = symbols.getMinusSign();
        boolean containsLocaleDecimal = text.indexOf(decimal) >= 0;

        StringBuilder builder = new StringBuilder();
        boolean digitSeen = false;
        boolean decimalSeen = false;
        boolean minusSeen = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isDigit(c)) {
                builder.append(c);
                digitSeen = true;
                continue;
            }

            if (c == grouping) {
                continue;
            }

            if (c == decimal) {
                if (decimalSeen) {
                    return null;
                }
                builder.append('.');
                decimalSeen = true;
                continue;
            }

            if (decimal != '.' && c == '.' && !containsLocaleDecimal) {
                if (decimalSeen) {
                    return null;
                }
                builder.append('.');
                decimalSeen = true;
                continue;
            }

            if (c == '-' || c == minus || c == '\u2212') {
                if (minusSeen || builder.length() > 0) {
                    return null;
                }
                builder.append('-');
                minusSeen = true;
                continue;
            }

            if (Character.isWhitespace(c)) {
                continue;
            }

            // Ignore other characters such as currency symbols.
        }

        if (!digitSeen) {
            return null;
        }

        String candidate = builder.toString();
        if (candidate.isEmpty() || "-".equals(candidate)) {
            return null;
        }

        try {
            return new BigDecimal(candidate);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
