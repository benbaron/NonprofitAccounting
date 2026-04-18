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

    public void saveAssetRecord(AssetRecord row) throws SQLException { assetRecordRepository.upsert(row); }
    public List<AssetRecord> listAssetRecords() throws SQLException { return assetRecordRepository.listAll(); }

    public void saveBankStatementRecord(BankStatementRecord row) throws SQLException { bankStatementRecordRepository.upsert(row); }
    public List<BankStatementRecord> listBankStatementRecords() throws SQLException { return bankStatementRecordRepository.listAll(); }

    public void saveBankingItemRecord(BankingItemRecord row) throws SQLException { bankingItemRecordRepository.upsert(row); }
    public List<BankingItemRecord> listBankingItemRecords() throws SQLException { return bankingItemRecordRepository.listAll(); }

    public void saveBudgetRecord(BudgetRecord row) throws SQLException { budgetRecordRepository.upsert(row); }
    public List<BudgetRecord> listBudgetRecords() throws SQLException { return budgetRecordRepository.listAll(); }

    public void saveDocumentRecord(DocumentRecord row) throws SQLException { documentRecordRepository.upsert(row); }
    public List<DocumentRecord> listDocumentRecords() throws SQLException { return documentRecordRepository.listAll(); }

    public void saveEventRecord(EventRecord row) throws SQLException { eventRecordRepository.upsert(row); }
    public List<EventRecord> listEventRecords() throws SQLException { return eventRecordRepository.listAll(); }

    public void saveFundRecord(FundRecord row) throws SQLException { fundRecordRepository.upsert(row); }
    public List<FundRecord> listFundRecords() throws SQLException { return fundRecordRepository.listAll(); }

    public void saveOrganizationRecord(OrganizationRecord row) throws SQLException { organizationRecordRepository.upsert(row); }
    public List<OrganizationRecord> listOrganizationRecords() throws SQLException { return organizationRecordRepository.listAll(); }

    public void saveOtherAssetItemRecord(OtherAssetItemRecord row) throws SQLException { otherAssetItemRecordRepository.upsert(row); }
    public List<OtherAssetItemRecord> listOtherAssetItemRecords() throws SQLException { return otherAssetItemRecordRepository.listAll(); }

    public void saveOutstandingItemRecord(OutstandingItemRecord row) throws SQLException { outstandingItemRecordRepository.upsert(row); }
    public List<OutstandingItemRecord> listOutstandingItemRecords() throws SQLException { return outstandingItemRecordRepository.listAll(); }

    public void saveReportingPeriodRecord(ReportingPeriodRecord row) throws SQLException { reportingPeriodRecordRepository.upsert(row); }
    public List<ReportingPeriodRecord> listReportingPeriodRecords() throws SQLException { return reportingPeriodRecordRepository.listAll(); }

    public void saveSupplyRecord(SupplyRecord row) throws SQLException { supplyRecordRepository.upsert(row); }
    public List<SupplyRecord> listSupplyRecords() throws SQLException { return supplyRecordRepository.listAll(); }

    public void addExcelLedgerRow(ExcelLedgerRow row)
    {
        synchronized (excelLedgerRows)
        {
            excelLedgerRows.add(row);
        }
    }

    public List<ExcelLedgerRow> listExcelLedgerRows()
    {
        synchronized (excelLedgerRows)
        {
            return List.copyOf(excelLedgerRows);
        }
    }

    public void addImportedTransaction(ImportedTransaction row)
    {
        synchronized (importedTransactions)
        {
            importedTransactions.add(row);
        }
    }

    public List<ImportedTransaction> listImportedTransactions()
    {
        synchronized (importedTransactions)
        {
            return List.copyOf(importedTransactions);
        }
    }
}
