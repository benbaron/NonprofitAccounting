package nonprofitbookkeeping.reports.jasper.runtime;

import java.util.function.Supplier;

/**
 * Thread-local holder for {@link ReportContext} during report data generation.
 */
public final class ReportContextHolder
{
	private static final ThreadLocal<ReportContext> CURRENT =
		new ThreadLocal<>();

	private ReportContextHolder()
	{
		
	}

	/**
	 * Returns the active {@link ReportContext} for the current thread.
	 *
	 * @return report context or {@code null} if none is set
	 */
	public static ReportContext get()
	{
		return CURRENT.get();
		
	}

	/**
	 * Executes the supplied work with the given context bound to the thread.
	 *
	 * @param context report context to bind for the duration of the supplier
	 * @param supplier work to execute
	 * @return supplier result
	 * @param <T> supplier return type
	 */
	public static <T> T withContext(ReportContext context,
		Supplier<T> supplier)
	{
		ReportContext previous = CURRENT.get();
		CURRENT.set(context);
		try
		{
			return supplier.get();
		}
		finally
		{
			if (previous == null)
			{
				CURRENT.remove();
			}
			else
			{
				CURRENT.set(previous);
			}
		}
		
	}
}
