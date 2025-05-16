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

public class SCALedgerDataLoader
{
    /**
     * Loads the spreadsheet data into Java objects using JXLS ReaderIntf.
     * 
     * @param mappingFile path to XML mapping file
     * @param currentInputFile path to the Excel file (.xlsx/.xlsm)
     * @return a Map of all extracted beans
     * @throws Exception on failure
     */
    public static Map<String, Object> loadData(File mappingFile, File currentInputFile) throws Exception
    {
        try (
            InputStream mappingXml = new BufferedInputStream(new FileInputStream(mappingFile));
            InputStream excelFile = new BufferedInputStream(new FileInputStream(currentInputFile))
        )
        {
        	ReaderConfig.getInstance().setSkipErrors( true );
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
                throw new IllegalStateException("JXLS failed to read the Excel file.");
            }

            return beans;
        }
    }
}
