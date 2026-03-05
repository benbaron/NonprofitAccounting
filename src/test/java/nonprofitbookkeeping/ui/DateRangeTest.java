package nonprofitbookkeeping.ui;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateRangeTest
{

    @Test
    void allRangeToStringAndIsAll()
    {
        DateRange range = DateRange.ALL;

        assertTrue(range.isAll());
        assertEquals("All Dates", range.toString());
    }

    @Test
    void boundedRangeToString()
    {
        DateRange range = new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertFalse(range.isAll());
        assertEquals("2026-01-01 to 2026-01-31", range.toString());
    }

    @Test
    void equalityAndHashCode()
    {
        DateRange a = new DateRange(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));
        DateRange b = new DateRange(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
