
package nonprofitbookkeeping.ui.actions.scaledger;

import org.jxls.reader.ReaderBuilder;
import org.jxls.reader.ReaderConfig;
import org.jxls.reader.XLSReadStatus;
import org.jxls.reader.XLSReader;

import nonprofitbookkeeping.model.Summary;
import nonprofitbookkeeping.model.scaledger.LedgerContainer;
import nonprofitbookkeeping.model.scaledger.LedgerMetadata;

import java.io.*;
import java.util.*;

/**
 * Utility class for loading data from SCA  formatted
 * Excel spreadsheets into Java objects using JXLS Reader.
 * It uses an XML mapping file to define how data from the Excel sheet
 * corresponds to Java bean properties.
 */
public class SCALedgerDataLoader
{
	/**
	 * Loads data from a specified Excel file (.xlsx or .xlsm) into a map of Java beans
	 * using a JXLS XML mapping file.
	 * <p>
	 * This method initializes {@link LedgerMetadata}, {@link Summary}, and {@link LedgerContainer}
	 * (with its quarterly ledger lists) and puts them into a "beans" map. The JXLS {@link XLSReader}
	 * then populates these beans based on the rules defined in the {@code mappingFile}
	 * and data from the {@code currentInputFile}.
	 * Error skipping is enabled in the JXLS reader configuration.
	 * </p>
	 * 
	 * @param mappingFile The JXLS XML mapping {@link File} that defines how to read the Excel data
	 *                    into the Java beans.
	 * @param currentInputFile The Excel {@link File} (.xlsx or .xlsm) containing the SCA ledger data.
	 * @return A {@link Map} where keys are bean names (e.g., "metadata", "summary", "ledgerQ1")
	 *         and values are the populated Java bean objects.
	 * @throws org.xml.sax.SAXException if there is an error parsing the JXLS mapping XML file.
	 * @throws java.io.FileNotFoundException if either {@code mappingFile} or {@code currentInputFile} is not found.
	 * @throws IOException if an I/O error occurs during reading of the files or JXLS processing.
	 * @throws IllegalStateException if JXLS fails to read the Excel file successfully ({@code XLSReadStatus.isStatusOK()} is false).
	 * @throws Exception for other potential errors during JXLS processing.
	 */
	public static Map<String, Object> loadData(File mappingFile,
		File currentInputFile) throws Exception
	{
		
		try (
			InputStream mappingXml =
				new BufferedInputStream(new FileInputStream(mappingFile)); // Can
																			// throw
																			// FileNotFoundException
			InputStream excelFile =
				new BufferedInputStream(new FileInputStream(currentInputFile)))
		{
			ReaderConfig.getInstance().setSkipErrors(true);
			XLSReader reader = ReaderBuilder.buildFromXML(mappingXml);
			
			LedgerMetadata metadata = new LedgerMetadata();
			Summary summary = new Summary();
			LedgerContainer ledgerContainer = new LedgerContainer();
			
			Map<String, Object> beans = new HashMap<>();
			beans.put("metadata", metadata);
			beans.put("summary", summary);
			beans.put("ledgerQ1", ledgerContainer.ledgerQ1);
			beans.put("ledgerQ2", ledgerContainer.ledgerQ2);
			beans.put("ledgerQ3", ledgerContainer.ledgerQ3);
			beans.put("ledgerQ4", ledgerContainer.ledgerQ4);
			
			XLSReadStatus status = reader.read(excelFile, beans);
			
			if (!status.isStatusOK())
			{
				throw new IllegalStateException(
					"JXLS failed to read the Excel file.");
			}
			
			return beans;
		}
		
	}
	
}
