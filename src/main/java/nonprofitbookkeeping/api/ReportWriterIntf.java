
package nonprofitbookkeeping.api;

import java.io.File;
import java.io.IOException;

import nonprofitbookkeeping.reports.ReportContext;

/**
 * 
 */
public interface ReportWriterIntf
{
	/**
	 * writeReport
	 * @param context Report Context
	 * @return Report File
	 * @throws IOException
	 * @throws Exception
	 */
	File writeReport(ReportContext context) throws IOException, Exception;
	
}
