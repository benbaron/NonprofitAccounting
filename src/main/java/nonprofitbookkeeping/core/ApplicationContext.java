package nonprofitbookkeeping.core;

import javafx.stage.Stage;
import javafx.scene.control.MenuBar; // Added for plugins to add menus
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany; // For default impl if needed, or direct access
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.BudgetService;
import nonprofitbookkeeping.service.ReportConfigurationService;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.service.DocumentStorageService;
import nonprofitbookkeeping.service.FundAccountingService;
// Assuming FileImportService and FileExportService are not in ServiceContainer,
// but they could be added if plugins need them.

public interface ApplicationContext {
    Stage getPrimaryStage();
    // MenuBar getMenuBar(); // Removed as per guidance
    Company getCurrentCompany(); 
    
    // Service Accessors
    ReportService getReportService();
    BudgetService getBudgetService();
    ReportConfigurationService getReportConfigurationService();
    InventoryService getInventoryService();
    DocumentStorageService getDocumentStorageService();
    FundAccountingService getFundAccountingService();

    // Utility for UI updates from non-FX threads if plugins use them
    void runLater(Runnable runnable);
}
