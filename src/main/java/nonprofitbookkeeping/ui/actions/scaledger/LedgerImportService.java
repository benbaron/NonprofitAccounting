package nonprofitbookkeeping.ui.actions.scaledger;

import nonprofitbookkeeping.model.AccountingTransaction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// TODO: Auto-generated Javadoc
/**
 * Coordinates loading ledger spreadsheets and persisting the mapped
 * transactions through the supplied persistence gateway.
 */
public class LedgerImportService
{
    
    /** The sheet importer. */
    private final LedgerSheetImporter sheetImporter;
    
    /** The mapper. */
    private final LedgerToDomainMapper mapper;
    
    /** The persistence gateway. */
    private final LedgerPersistenceGateway persistenceGateway;

    /**
     * Instantiates a new ledger import service.
     *
     * @param persistenceGateway the persistence gateway
     */
    public LedgerImportService(LedgerPersistenceGateway persistenceGateway)
    {
        this(new LedgerSheetImporter(), new LedgerToDomainMapper(), persistenceGateway);
    }

    /**
     * Instantiates a new ledger import service.
     *
     * @param sheetImporter the sheet importer
     * @param mapper the mapper
     * @param persistenceGateway the persistence gateway
     */
    LedgerImportService(LedgerSheetImporter sheetImporter,
                        LedgerToDomainMapper mapper,
                        LedgerPersistenceGateway persistenceGateway)
    {
        this.sheetImporter = Objects.requireNonNull(sheetImporter, "sheetImporter");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.persistenceGateway = Objects.requireNonNull(persistenceGateway, "persistenceGateway");
    }

    /**
     * Import a workbook sheet and persist the resulting transactions.
     *
     * @param chartMapFile optional path to the chart translation map JSON; may be {@code null}
     * @param workbookFile Excel workbook path
     * @param sheetName    sheet within the workbook to read
     * @return list of persisted transactions in the order they were saved
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public List<AccountingTransaction> importAndPersist(Path chartMapFile,
                                                        Path workbookFile,
                                                        String sheetName) throws IOException
    {
        Objects.requireNonNull(workbookFile, "workbookFile");
        Objects.requireNonNull(sheetName, "sheetName");

        ChartTranslationMap translation = null;
        if (chartMapFile != null)
        {
            translation = ChartTranslationMap.fromJsonFile(chartMapFile);
        }

        return importAndPersist(workbookFile, sheetName, translation);
    }

    /**
     * Import using a translation map that was loaded through an alternate mechanism
     * (for example, from the classpath).
     *
     * @param workbookFile the workbook file
     * @param sheetName the sheet name
     * @param translation the translation
     * @return the list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public List<AccountingTransaction> importAndPersist(Path workbookFile,
                                                        String sheetName,
                                                        ChartTranslationMap translation) throws IOException
    {
        Objects.requireNonNull(workbookFile, "workbookFile");
        Objects.requireNonNull(sheetName, "sheetName");

        LedgerQuarter quarter = this.sheetImporter.importQuarter(workbookFile, sheetName, translation);
        if (quarter == null || quarter.getRows().isEmpty())
        {
            return Collections.emptyList();
        }

        List<AccountingTransaction> persisted = new ArrayList<>();

        for (LedgerRow row : quarter.getRows())
        {
            if (row == null || row.isEffectivelyBlank())
            {
                continue;
            }

            AccountingTransaction transaction = this.mapper.mapRowToTransaction(row, quarter.getSheetName());
            AccountingTransaction saved = this.persistenceGateway.saveTransactionWithEntries(transaction);
            if (saved != null)
            {
                persisted.add(saved);
            }
        }

        return persisted;
    }
}
