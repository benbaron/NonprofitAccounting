package nonprofitbookkeeping.ui;

import nonprofitbookkeeping.service.AssetRecordService;
import nonprofitbookkeeping.service.BankStatementRecordService;
import nonprofitbookkeeping.service.BankingItemRecordService;
import nonprofitbookkeeping.service.BudgetRecordService;
import nonprofitbookkeeping.service.DocumentRecordService;
import nonprofitbookkeeping.service.EventRecordService;
import nonprofitbookkeeping.service.ExcelLedgerRowService;
import nonprofitbookkeeping.service.FundRecordService;
import nonprofitbookkeeping.service.GrantRecordService;
import nonprofitbookkeeping.service.ImportedTransactionService;
import nonprofitbookkeeping.service.OrganizationRecordService;
import nonprofitbookkeeping.service.OtherAssetItemRecordService;
import nonprofitbookkeeping.service.OutstandingItemRecordService;
import nonprofitbookkeeping.service.ReportingPeriodRecordService;
import nonprofitbookkeeping.service.SupplyRecordService;
import org.nonprofitbookkeeping.ui.AppPanelId;
import org.nonprofitbookkeeping.ui.AppPanel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Defines UI-panel bindings for each promoted record service.
 */
public final class RecordServicePanelRegistry
{
    private static final Map<Class<?>, PanelBinding> BINDINGS = new LinkedHashMap<>();

    static
    {
        bind(AssetRecordService.class, "Asset Records", "Assets", AppPanelId.ASSETS_REGISTER, false, AssetsRegisterPanel::new);
        bind(SupplyRecordService.class, "Supply Records", "Assets", AppPanelId.ASSETS_REGISTER, false, AssetsRegisterPanel::new);
        bind(OtherAssetItemRecordService.class, "Other Asset Records", "Assets", AppPanelId.ASSETS_REGISTER, false, AssetsRegisterPanel::new);

        bind(BudgetRecordService.class, "Budget Records", "Budget", AppPanelId.BUDGET_EDITOR, false, BudgetEditorPanel::new);
        bind(FundRecordService.class, "Fund Records", "Budget", AppPanelId.BUDGET_VS_ACTUAL, false, FundRecordsPanel::new);
        bind(GrantRecordService.class, "Grant Records", "Budget", AppPanelId.BUDGET_VS_ACTUAL, false, GrantRecordsPanel::new);

        bind(BankStatementRecordService.class, "Bank Statement Records", "Banking", AppPanelId.LEDGER_REGISTER, false, BankStatementRecordsPanel::new);
        bind(BankingItemRecordService.class, "Banking Item Records", "Banking", AppPanelId.LEDGER_REGISTER, false, LedgerRegisterPanel::new);
        bind(ImportedTransactionService.class, "Imported Transaction Records", "Banking", AppPanelId.LEDGER_REGISTER, false,
            LedgerRegisterPanel::new);
        bind(ExcelLedgerRowService.class, "Excel Ledger Row Records", "Banking", AppPanelId.LEDGER_REGISTER, false, LedgerRegisterPanel::new);

        bind(DocumentRecordService.class, "Document Records", "Admin", AppPanelId.SETTINGS, false,
            () -> new GenericRecordEditorPanel("Document Records", "imported_document_record", "document_id", () -> UUID.randomUUID().toString()));
        bind(EventRecordService.class, "Event Records", "Admin", AppPanelId.SETTINGS, false,
            () -> new GenericRecordEditorPanel("Event Records", "imported_event_record", "event_id", () -> UUID.randomUUID().toString()));
        bind(OrganizationRecordService.class, "Organization Records", "Admin", AppPanelId.SETTINGS, false,
            () -> new GenericRecordEditorPanel("Organization Records", "imported_organization_record", "organization_id", () -> UUID.randomUUID().toString()));
        bind(OutstandingItemRecordService.class, "Outstanding Item Records", "Banking", AppPanelId.LEDGER_REGISTER, false,
            () -> new GenericRecordEditorPanel("Outstanding Item Records", "imported_outstanding_item_record", "outstanding_item_id", () -> UUID.randomUUID().toString()));
        bind(ReportingPeriodRecordService.class, "Reporting Period Records", "Admin", AppPanelId.SETTINGS, false,
            () -> new GenericRecordEditorPanel("Reporting Period Records", "imported_reporting_period_record", "period_key", () -> UUID.randomUUID().toString()));
    }

    private RecordServicePanelRegistry()
    {
    }

    public static Optional<PanelBinding> lookup(Class<?> serviceType)
    {
        return Optional.ofNullable(BINDINGS.get(serviceType));
    }

    public static Map<Class<?>, PanelBinding> all()
    {
        return Map.copyOf(BINDINGS);
    }

    private static void bind(Class<?> serviceType,
        String displayName,
        String category,
        AppPanelId workspacePanelId,
        boolean proposed,
        Supplier<AppPanel> panelFactory)
    {
        BINDINGS.put(serviceType, new PanelBinding(serviceType, displayName, category, workspacePanelId, proposed,
            panelFactory));
    }

    /**
     * Binding descriptor between a record service and a UI panel.
     */
    public record PanelBinding(
        Class<?> serviceType,
        String displayName,
        String category,
        AppPanelId workspacePanelId,
        boolean proposedPanel,
        Supplier<AppPanel> panelFactory)
    {
    }
}
