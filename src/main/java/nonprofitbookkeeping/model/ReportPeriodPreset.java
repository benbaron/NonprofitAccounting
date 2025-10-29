package nonprofitbookkeeping.model;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.Locale;

/**
 * Preset periods that can be applied to reporting and analytical views.
 */
public enum ReportPeriodPreset
{
        /** Beginning of the fiscal year through the reference date. */
        YEAR_TO_DATE,
        /** The full fiscal year containing the reference date. */
        FULL_YEAR,
        /** The immediate calendar month prior to the reference date. */
        LAST_MONTH;

        /**
         * Resolves this preset into an inclusive date range.
         *
         * @param referenceDate    date used as the anchor for calculations
         * @param fiscalYearStart  optional month/day representing the beginning of the fiscal year
         * @return a {@link DateRange} covering the requested period
         */
        public DateRange resolve(LocalDate referenceDate, MonthDay fiscalYearStart)
        {
                LocalDate today = referenceDate != null ? referenceDate : LocalDate.now();

                switch (this)
                {
                        case YEAR_TO_DATE:
                                return new DateRange(startOfFiscalYear(today, fiscalYearStart), today);

                        case FULL_YEAR:
                        {
                                LocalDate start = startOfFiscalYear(today, fiscalYearStart);
                                return new DateRange(start, start.plusYears(1).minusDays(1));
                        }

                        case LAST_MONTH:
                        {
                                YearMonth previous = YearMonth.from(today).minusMonths(1);
                                LocalDate start = previous.atDay(1);
                                return new DateRange(start, previous.atEndOfMonth());
                        }

                        default:
                                return new DateRange(today, today);
                }
        }

        private static LocalDate startOfFiscalYear(LocalDate referenceDate, MonthDay fiscalStart)
        {
                if (fiscalStart == null)
                {
                        return LocalDate.of(referenceDate.getYear(), 1, 1);
                }

                LocalDate candidate = fiscalStart.atYear(referenceDate.getYear());

                if (candidate.isAfter(referenceDate))
                {
                        candidate = fiscalStart.atYear(referenceDate.getYear() - 1);
                }

                return candidate;
        }

        /** Simple value object describing a closed date range. */
        public static final class DateRange
        {
                private final LocalDate start;
                private final LocalDate end;

                public DateRange(LocalDate start, LocalDate end)
                {
                        this.start = start;
                        this.end = end;
                }

                public LocalDate getStart()
                {
                        return this.start;
                }

                public LocalDate getEnd()
                {
                        return this.end;
                }
        }

        /**
         * Attempts to parse the supplied text into a {@link ReportPeriodPreset}.
         *
         * @param value    text representation, typically {@link Enum#name()}
         * @param fallback value used when {@code value} is {@code null} or cannot be parsed
         * @return resolved preset
         */
        public static ReportPeriodPreset fromString(String value, ReportPeriodPreset fallback)
        {
                if (value == null || value.isBlank())
                {
                        return fallback;
                }

                try
                {
                        return ReportPeriodPreset.valueOf(value.trim().toUpperCase(Locale.ROOT));
                }
                catch (IllegalArgumentException ex)
                {
                        return fallback;
                }
        }
}
