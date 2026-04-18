package nonprofitbookkeeping.ui;

import nonprofitbookkeeping.service.AssetRecordService;
import nonprofitbookkeeping.service.BankStatementRecordService;
import nonprofitbookkeeping.service.BankingItemRecordService;
import nonprofitbookkeeping.service.BudgetRecordService;
import nonprofitbookkeeping.service.DocumentRecordService;
import nonprofitbookkeeping.service.EventRecordService;
import nonprofitbookkeeping.service.ExcelLedgerRowService;
import nonprofitbookkeeping.service.FundRecordService;
import nonprofitbookkeeping.service.ImportedTransactionService;
import nonprofitbookkeeping.service.OrganizationRecordService;
import nonprofitbookkeeping.service.OtherAssetItemRecordService;
import nonprofitbookkeeping.service.OutstandingItemRecordService;
import nonprofitbookkeeping.service.ReportingPeriodRecordService;
import nonprofitbookkeeping.service.SupplyRecordService;
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
        bind(AssetRecordService.class, false, AssetsRegisterPanel::new);
        bind(BudgetRecordService.class, false, BudgetEditorPanel::new);
        bind(FundRecordService.class, false, BudgetVsActualPanel::new);

        bind(BankStatementRecordService.class, false, LedgerRegisterPanel::new);
        bind(BankingItemRecordService.class, false, LedgerRegisterPanel::new);
        bind(ImportedTransactionService.class, false, LedgerRegisterPanel::new);
        bind(ExcelLedgerRowService.class, false, LedgerRegisterPanel::new);

        bind(SupplyRecordService.class, false, AssetsRegisterPanel::new);
        bind(OtherAssetItemRecordService.class, false, AssetsRegisterPanel::new);

        bind(DocumentRecordService.class, true,
            () -> new ProposedRecordPanel("Document Records",
                "Proposed panel: browse linked documents, metadata, and attachment status."));
        bind(EventRecordService.class, true,
            () -> new ProposedRecordPanel("Event Records",
                "Proposed panel: event schedule, host org linkage, and period rollups."));
        bind(OrganizationRecordService.class, true,
            () -> new ProposedRecordPanel("Organization Records",
                "Proposed panel: organization hierarchy and fiscal/calendar controls."));
        bind(OutstandingItemRecordService.class, true,
            () -> new ProposedRecordPanel("Outstanding Items",
                "Proposed panel: open checks/transfers with reconciliation lifecycle actions."));
        bind(ReportingPeriodRecordService.class, true,
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

    private static void bind(Class<?> serviceType, boolean proposed, Supplier<AppPanel> panelFactory)
    {
        BINDINGS.put(serviceType, new PanelBinding(serviceType, proposed, panelFactory));
    }

    /**
     * Binding descriptor between a record service and a UI panel.
     */
    public record PanelBinding(
        Class<?> serviceType,
        boolean proposedPanel,
        Supplier<AppPanel> panelFactory)
    {
    }
}
