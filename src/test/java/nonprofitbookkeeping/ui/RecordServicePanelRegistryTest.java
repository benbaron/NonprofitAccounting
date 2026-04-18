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
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordServicePanelRegistryTest
{
    @Test
    void everyRecordServiceHasPanelBinding()
    {
        List<Class<?>> services = List.of(
            AssetRecordService.class,
            BankStatementRecordService.class,
            BankingItemRecordService.class,
            BudgetRecordService.class,
            DocumentRecordService.class,
            EventRecordService.class,
            ExcelLedgerRowService.class,
            FundRecordService.class,
            ImportedTransactionService.class,
            OrganizationRecordService.class,
            OtherAssetItemRecordService.class,
            OutstandingItemRecordService.class,
            ReportingPeriodRecordService.class,
            SupplyRecordService.class
        );

        for (Class<?> service : services)
        {
            var bindingOpt = RecordServicePanelRegistry.lookup(service);
            assertTrue(bindingOpt.isPresent(), "Missing panel binding for: " + service.getSimpleName());
            assertTrue(bindingOpt.get().panelFactory() != null,
                "Panel factory should be present for: " + service.getSimpleName());
            assertFalse(bindingOpt.get().displayName().isBlank(),
                "Display name should be present for: " + service.getSimpleName());
            assertFalse(bindingOpt.get().category().isBlank(),
                "Category should be present for: " + service.getSimpleName());
            if (bindingOpt.get().proposedPanel())
            {
                assertTrue(bindingOpt.get().workspacePanelId() == null,
                    "Proposed panels should not require a workspace panel id: " + service.getSimpleName());
            }
            else
            {
                assertTrue(bindingOpt.get().workspacePanelId() != null,
                    "Existing panels should route to a workspace panel id: " + service.getSimpleName());
            }
        }
    }
}
