package nonprofitbookkeeping.core;

import javafx.application.Platform;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany; 
import nonprofitbookkeeping.service.*; // Import all from service

public class ApplicationContextImpl implements ApplicationContext {

    private final Stage primaryStage;
    // private final MenuBar menuBar; // Removed
    private final ReportService reportService;
    private final BudgetService budgetService;
    private final ReportConfigurationService reportConfigurationService;
    private final InventoryService inventoryService;
    private final DocumentStorageService documentStorageService;
    private final FundAccountingService fundAccountingService;
    // Add FileImportService, FileExportService if they become part of ServiceContainer
    // For now, assuming they are not managed singletons in ServiceContainer.

    public ApplicationContextImpl(Stage primaryStage,
                                  // MenuBar menuBar, // Removed
                                  ReportService reportService,
                                  BudgetService budgetService,
                                  ReportConfigurationService reportConfigurationService,
                                  InventoryService inventoryService,
                                  DocumentStorageService documentStorageService,
                                  FundAccountingService fundAccountingService) {
        this.primaryStage = primaryStage;
        // this.menuBar = menuBar; // Removed
        this.reportService = reportService;
        this.budgetService = budgetService;
        this.reportConfigurationService = reportConfigurationService;
        this.inventoryService = inventoryService;
        this.documentStorageService = documentStorageService;
        this.fundAccountingService = fundAccountingService;
    }

    @Override
    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    // @Override
    // public MenuBar getMenuBar() { // Removed
    //     return this.menuBar;
    // }

    @Override
    public Company getCurrentCompany() {
        return CurrentCompany.getCompany(); 
    }

    @Override
    public ReportService getReportService() {
        return this.reportService;
    }

    @Override
    public BudgetService getBudgetService() {
        return this.budgetService;
    }

    @Override
    public ReportConfigurationService getReportConfigurationService() {
        return this.reportConfigurationService;
    }

    @Override
    public InventoryService getInventoryService() {
        return this.inventoryService;
    }

    @Override
    public DocumentStorageService getDocumentStorageService() {
        return this.documentStorageService;
    }

    @Override
    public FundAccountingService getFundAccountingService() {
        return this.fundAccountingService;
    }

    @Override
    public void runLater(Runnable runnable) {
        if (runnable == null) return;
        // Ensure JavaFX platform is running before calling Platform.runLater()
        // This might not be strictly necessary if ApplicationContext is only used
        // after JavaFX has started, but it's a good safeguard.
        try {
            Platform.runLater(runnable);
        } catch (IllegalStateException e) {
            // JavaFX Platform not initialized yet, run directly or queue differently
            // For now, log and run directly if in a context where this might happen (e.g. early tests)
            System.err.println("Warning: JavaFX Platform not running. Executing runnable directly. " + e.getMessage());
            runnable.run(); 
        }
    }
}
