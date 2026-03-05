package nonprofitbookkeeping.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Shared context for the currently-selected date range.
 */
public final class DateRangeContext
{

	private static final ObjectProperty<DateRange> selected =
		new SimpleObjectProperty<>(DateRange.ALL);

	private DateRangeContext()
	{
	}

	public static ObjectProperty<DateRange> selectedProperty()
	{
		return selected;
	}

	public static DateRange get()
	{
		return selected.get();
	}

	public static void set(DateRange range)
	{
		selected.set(range == null ? DateRange.ALL : range);
	}

}
