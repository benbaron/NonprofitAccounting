
package nonprofitbookkeeping.core;

import javafx.application.Platform;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.*; // Import all from service

public class ApplicationContextImpl implements ApplicationContext
{
	
	private final Stage primaryStage;
	private final MenuBar menuBar;
	private final ReportService reportService;
	private final BudgetService budgetService;
	private final ReportConfigurationService reportConfigurationService;
	private final InventoryService inventoryService;
	private final DocumentStorageService documentStorageService;
	private final FundAccountingService fundAccountingService;
	// Add FileImportService, FileExportService if they become part of
	// ServiceContainer
	// For now, assuming they are not managed singletons in ServiceContainer.
	
	public ApplicationContextImpl(Stage primaryStage,
		MenuBar menuBar,
		ReportService reportService,
		BudgetService budgetService,
		ReportConfigurationService reportConfigurationService,
		InventoryService inventoryService,
		DocumentStorageService documentStorageService,
		FundAccountingService fundAccountingService)
	{
		
		if (primaryStage == null)
		{
			throw new IllegalArgumentException("PrimaryStage cannot be null.");
		}
		
		if (menuBar == null)
		{
			throw new IllegalArgumentException("MenuBar cannot be null.");
		}
		// Null checks for other services would also be good practice here,
		// but are outside the scope of this specific subtask.
		
		this.primaryStage = primaryStage;
		this.menuBar = menuBar;
		this.reportService = reportService;
		this.budgetService = budgetService;
		this.reportConfigurationService = reportConfigurationService;
		this.inventoryService = inventoryService;
		this.documentStorageService = documentStorageService;
		this.fundAccountingService = fundAccountingService;
	}
	
	/**  
	 * Constructor ApplicationContextImpl
	 * @param primaryStage2
	 * @param reportservice2
	 * @param budgetservice2
	 * @param reportconfigurationservice2
	 * @param iss
	 * @param dss
	 * @param fas
	 */
	public ApplicationContextImpl(Stage primaryStage2,
		ReportService reportservice2,
		BudgetService budgetservice2,
		ReportConfigurationService reportconfigurationservice2,
		InventoryService iss,
		DocumentStorageService dss,
		FundAccountingService fas)
	{
		this.primaryStage = new Stage();
		this.menuBar = new MenuBar();
		this.reportService = new ReportService();
		this.budgetService = new BudgetService();
		this.reportConfigurationService = new ReportConfigurationService();
		this.inventoryService = new InventoryService();
		this.documentStorageService = new DocumentStorageService();
		this.fundAccountingService = new FundAccountingService();
	}
	
	@Override public Stage getPrimaryStage()
	{
		return this.primaryStage;
	}
	
	// @Override
	// public MenuBar getMenuBar() { // This comment block can be removed
	// return this.menuBar;
	// }
	
	@Override public Company getCurrentCompany()
	{
		return CurrentCompany.getCompany();
	}
	
	@Override public ReportService getReportService()
	{
		return this.reportService;
	}
	
	@Override public BudgetService getBudgetService()
	{
		return this.budgetService;
	}
	
	@Override public ReportConfigurationService getReportConfigurationService()
	{
		return this.reportConfigurationService;
	}
	
	@Override public InventoryService getInventoryService()
	{
		return this.inventoryService;
	}
	
	@Override public DocumentStorageService getDocumentStorageService()
	{
		return this.documentStorageService;
	}
	
	@Override public FundAccountingService getFundAccountingService()
	{
		return this.fundAccountingService;
	}
	
	@Override public void runLater(Runnable runnable)
	{
		if (runnable == null)
			return;
			
		// Ensure JavaFX platform is running before calling Platform.runLater()
		// This might not be strictly necessary if ApplicationContext is only used
		// after JavaFX has started, but it's a good safeguard.
		try
		{
			Platform.runLater(runnable);
		}
		catch (IllegalStateException e)
		{
			// JavaFX Platform not initialized yet, run directly or queue differently
			// For now, log and run directly if in a context where this might happen (e.g.
			// early tests)
			System.err
				.println("Warning: JavaFX Platform not running. Executing runnable directly. " +
					e.getMessage());
			runnable.run();
		}
		
	}
	
	/**
	 * Gets the main MenuBar for the application.
	 * Implements {@link ApplicationContext#getMenuBar()}.
	 * @return The application's main {@link MenuBar}.
	 */
	@Override public MenuBar getMenuBar()
	{
		return this.menuBar;
	}
	
}
