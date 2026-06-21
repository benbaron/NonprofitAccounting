package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.records.AssetRecord;
import nonprofitbookkeeping.model.records.BankStatementRecord;
import nonprofitbookkeeping.model.records.BankingItemRecord;
import nonprofitbookkeeping.model.records.BudgetRecord;
import nonprofitbookkeeping.model.records.DocumentRecord;
import nonprofitbookkeeping.model.records.EventRecord;
import nonprofitbookkeeping.model.records.ExcelLedgerRow;
import nonprofitbookkeeping.model.records.FundRecord;
import nonprofitbookkeeping.model.records.ImportedTransaction;
import nonprofitbookkeeping.model.records.OrganizationRecord;
import nonprofitbookkeeping.model.records.OtherAssetItemRecord;
import nonprofitbookkeeping.model.records.OutstandingItemRecord;
import nonprofitbookkeeping.model.records.ReportingPeriodRecord;
import nonprofitbookkeeping.model.records.SupplyRecord;
import nonprofitbookkeeping.persistence.records.AssetRecordRepository;
import nonprofitbookkeeping.persistence.records.BankStatementRecordRepository;
import nonprofitbookkeeping.persistence.records.BankingItemRecordRepository;
import nonprofitbookkeeping.persistence.records.BudgetRecordRepository;
import nonprofitbookkeeping.persistence.records.DocumentRecordRepository;
import nonprofitbookkeeping.persistence.records.EventRecordRepository;
import nonprofitbookkeeping.persistence.records.FundRecordRepository;
import nonprofitbookkeeping.persistence.records.OrganizationRecordRepository;
import nonprofitbookkeeping.persistence.records.OtherAssetItemRecordRepository;
import nonprofitbookkeeping.persistence.records.OutstandingItemRecordRepository;
import nonprofitbookkeeping.persistence.records.ReportingPeriodRecordRepository;
import nonprofitbookkeeping.persistence.records.SupplyRecordRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * First-class service facade for records previously treated as import/export-only items.
 */
public class ImpexRecordService
{
    private final AssetRecordRepository assetRecordRepository;
    private final BankStatementRecordRepository bankStatementRecordRepository;
    private final BankingItemRecordRepository bankingItemRecordRepository;
    private final BudgetRecordRepository budgetRecordRepository;
    private final DocumentRecordRepository documentRecordRepository;
    private final EventRecordRepository eventRecordRepository;
    private final FundRecordRepository fundRecordRepository;
    private final OrganizationRecordRepository organizationRecordRepository;
    private final OtherAssetItemRecordRepository otherAssetItemRecordRepository;
    private final OutstandingItemRecordRepository outstandingItemRecordRepository;
    private final ReportingPeriodRecordRepository reportingPeriodRecordRepository;
    private final SupplyRecordRepository supplyRecordRepository;

    private final List<ExcelLedgerRow> excelLedgerRows = new ArrayList<>();
    private final List<ImportedTransaction> importedTransactions = new ArrayList<>();

    public ImpexRecordService()
    {
        this(
            new AssetRecordRepository(),
            new BankStatementRecordRepository(),
            new BankingItemRecordRepository(),
            new BudgetRecordRepository(),
            new DocumentRecordRepository(),
            new EventRecordRepository(),
            new FundRecordRepository(),
            new OrganizationRecordRepository(),
            new OtherAssetItemRecordRepository(),
            new OutstandingItemRecordRepository(),
            new ReportingPeriodRecordRepository(),
            new SupplyRecordRepository());
    }

    public ImpexRecordService(
        AssetRecordRepository assetRecordRepository,
        BankStatementRecordRepository bankStatementRecordRepository,
        BankingItemRecordRepository bankingItemRecordRepository,
        BudgetRecordRepository budgetRecordRepository,
        DocumentRecordRepository documentRecordRepository,
        EventRecordRepository eventRecordRepository,
        FundRecordRepository fundRecordRepository,
        OrganizationRecordRepository organizationRecordRepository,
        OtherAssetItemRecordRepository otherAssetItemRecordRepository,
        OutstandingItemRecordRepository outstandingItemRecordRepository,
        ReportingPeriodRecordRepository reportingPeriodRecordRepository,
        SupplyRecordRepository supplyRecordRepository)
    {
        this.assetRecordRepository = assetRecordRepository;
        this.bankStatementRecordRepository = bankStatementRecordRepository;
        this.bankingItemRecordRepository = bankingItemRecordRepository;
        this.budgetRecordRepository = budgetRecordRepository;
        this.documentRecordRepository = documentRecordRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.fundRecordRepository = fundRecordRepository;
        this.organizationRecordRepository = organizationRecordRepository;
        this.otherAssetItemRecordRepository = otherAssetItemRecordRepository;
        this.outstandingItemRecordRepository = outstandingItemRecordRepository;
        this.reportingPeriodRecordRepository = reportingPeriodRecordRepository;
        this.supplyRecordRepository = supplyRecordRepository;
    }

    public void saveAssetRecord(AssetRecord row) throws SQLException { this.assetRecordRepository.upsert(row); }
    public List<AssetRecord> listAssetRecords() throws SQLException { return this.assetRecordRepository.listAll(); }

    public void saveBankStatementRecord(BankStatementRecord row) throws SQLException { this.bankStatementRecordRepository.upsert(row); }
    public List<BankStatementRecord> listBankStatementRecords() throws SQLException { return this.bankStatementRecordRepository.listAll(); }

    public void saveBankingItemRecord(BankingItemRecord row) throws SQLException { this.bankingItemRecordRepository.upsert(row); }
    public List<BankingItemRecord> listBankingItemRecords() throws SQLException { return this.bankingItemRecordRepository.listAll(); }

    public void saveBudgetRecord(BudgetRecord row) throws SQLException { this.budgetRecordRepository.upsert(row); }
    public List<BudgetRecord> listBudgetRecords() throws SQLException { return this.budgetRecordRepository.listAll(); }

    public void saveDocumentRecord(DocumentRecord row) throws SQLException { this.documentRecordRepository.upsert(row); }
    public List<DocumentRecord> listDocumentRecords() throws SQLException { return this.documentRecordRepository.listAll(); }

    public void saveEventRecord(EventRecord row) throws SQLException { this.eventRecordRepository.upsert(row); }
    public List<EventRecord> listEventRecords() throws SQLException { return this.eventRecordRepository.listAll(); }

    public void saveFundRecord(FundRecord row) throws SQLException { this.fundRecordRepository.upsert(row); }
    public List<FundRecord> listFundRecords() throws SQLException { return this.fundRecordRepository.listAll(); }

    public void saveOrganizationRecord(OrganizationRecord row) throws SQLException { this.organizationRecordRepository.upsert(row); }
    public List<OrganizationRecord> listOrganizationRecords() throws SQLException { return this.organizationRecordRepository.listAll(); }

    public void saveOtherAssetItemRecord(OtherAssetItemRecord row) throws SQLException { this.otherAssetItemRecordRepository.upsert(row); }
    public List<OtherAssetItemRecord> listOtherAssetItemRecords() throws SQLException { return this.otherAssetItemRecordRepository.listAll(); }

    public void saveOutstandingItemRecord(OutstandingItemRecord row) throws SQLException { this.outstandingItemRecordRepository.upsert(row); }
    public List<OutstandingItemRecord> listOutstandingItemRecords() throws SQLException { return this.outstandingItemRecordRepository.listAll(); }

    public void saveReportingPeriodRecord(ReportingPeriodRecord row) throws SQLException { this.reportingPeriodRecordRepository.upsert(row); }
    public List<ReportingPeriodRecord> listReportingPeriodRecords() throws SQLException { return this.reportingPeriodRecordRepository.listAll(); }

    public void saveSupplyRecord(SupplyRecord row) throws SQLException { this.supplyRecordRepository.upsert(row); }
    public List<SupplyRecord> listSupplyRecords() throws SQLException { return this.supplyRecordRepository.listAll(); }

    public void addExcelLedgerRow(ExcelLedgerRow row)
    {
        synchronized (this.excelLedgerRows)
        {
            this.excelLedgerRows.add(row);
        }
    }

    public List<ExcelLedgerRow> listExcelLedgerRows()
    {
        synchronized (this.excelLedgerRows)
        {
            return List.copyOf(this.excelLedgerRows);
        }
    }

    public void addImportedTransaction(ImportedTransaction row)
    {
        synchronized (this.importedTransactions)
        {
            this.importedTransactions.add(row);
        }
    }

    public List<ImportedTransaction> listImportedTransactions()
    {
        synchronized (this.importedTransactions)
        {
            return List.copyOf(this.importedTransactions);
        }
    }
}
