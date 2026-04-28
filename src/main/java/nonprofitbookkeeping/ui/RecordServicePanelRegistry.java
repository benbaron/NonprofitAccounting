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

        bind(DocumentRecordService.class, "Document Records", "Admin", null, true,
            () -> new ProposedRecordPanel("Document Records",
                "Proposed panel: browse linked documents, metadata, and attachment status."));
        bind(EventRecordService.class, "Event Records", "Admin", null, true,
            () -> new ProposedRecordPanel("Event Records",
                "Proposed panel: event schedule, host org linkage, and period rollups."));
        bind(OrganizationRecordService.class, "Organization Records", "Admin", null, true,
            () -> new ProposedRecordPanel("Organization Records",
                "Proposed panel: organization hierarchy and fiscal/calendar controls."));
        bind(OutstandingItemRecordService.class, "Outstanding Item Records", "Banking", null, true,
            () -> new ProposedRecordPanel("Outstanding Items",
                "Proposed panel: open checks/transfers with reconciliation lifecycle actions."));
        bind(ReportingPeriodRecordService.class, "Reporting Period Records", "Admin", null, true,
            () -> new ProposedRecordPanel("Reporting Periods",
                "Proposed panel: fiscal periods, close/open workflow, and validation."));
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
