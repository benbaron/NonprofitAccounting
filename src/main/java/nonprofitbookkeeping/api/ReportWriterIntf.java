
package nonprofitbookkeeping.api;

import java.io.File;
import java.io.IOException;

import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;

/**
 * Defines the contract for report writers.
 * Implementations of this interface are responsible for generating a report
 * based on the provided context and writing it to a file.
 */
public interface ReportWriterIntf
{
	/**
	 * Writes a report based on the provided report context.
	 *
	 * @param context The context containing data and configuration for the report.
	 * @return A {@link File} object representing the generated report file.
	 * @throws IOException If an I/O error occurs during report writing.
	 * @throws Exception If any other error occurs during report generation.
	 */
	File writeReport(ReportContext context) throws IOException, Exception;
	
}
