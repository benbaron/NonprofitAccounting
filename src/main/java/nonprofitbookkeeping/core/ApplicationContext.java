
package nonprofitbookkeeping.core;

import javafx.stage.Stage;
import javafx.scene.control.MenuBar; // Added for plugins to add menus
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.service.BudgetService;
import nonprofitbookkeeping.service.ReportConfigurationService;
import nonprofitbookkeeping.service.InventoryService;
import nonprofitbookkeeping.service.DocumentStorageService;
import nonprofitbookkeeping.service.FundAccountingService;
// Assuming FileImportService and FileExportService
// are not in ServiceContainer,
// but they could be added if plugins need them.

public interface ApplicationContext
{
	/**
	 * Gets the primary stage of the application.
	 * @return The primary stage.
	 */
	Stage getPrimaryStage();
	
	// MenuBar getMenuBar(); // Removed as per guidance
	/**
	 * Gets the currently active company.
	 * @return The current company.
	 */
	Company getCurrentCompany();
	
	// Service Accessors
	/**
	 * Gets the report service.
	 * @return The report service.
	 */
	ReportService getReportService();
	
	/**
	 * Gets the budget service.
	 * @return The budget service.
	 */
	BudgetService getBudgetService();
	
	/**
	 * Gets the report configuration service.
	 * @return The report configuration service.
	 */
	ReportConfigurationService getReportConfigurationService();
	
	/**
	 * Gets the inventory service.
	 * @return The inventory service.
	 */
	InventoryService getInventoryService();
	
	/**
	 * Gets the document storage service.
	 * @return The document storage service.
	 */
	DocumentStorageService getDocumentStorageService();
	
	/**
	 * Gets the fund accounting service.
	 * @return The fund accounting service.
	 */
	FundAccountingService getFundAccountingService();
	
	// Utility for UI updates from non-FX threads if plugins use them
	/**
	 * Runs the specified {@link Runnable} on the JavaFX application thread at some unspecified time in the future.
	 * @param runnable The runnable to be executed.
	 */
	void runLater(Runnable runnable);
	
	/**
	 * Gets the menu bar of the application.
	 * @return The menu bar.
	 */
	MenuBar getMenuBar();
	
}
