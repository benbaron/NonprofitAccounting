
package nonprofitbookkeeping.core;

import javafx.application.Platform;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.service.*; // Import all from service

// TODO: Auto-generated Javadoc
/**
 * The Class ApplicationContextImpl.
 */
public class ApplicationContextImpl implements ApplicationContext
{
	
	/** The primary stage. */
	private final Stage primaryStage;
	
	/** The menu bar. */
	private final MenuBar menuBar;
	
	/** The report service. */
	private final ReportService reportService;
	
	/** The report configuration service. */
	private final ReportConfigurationService reportConfigurationService;
	
	/** The inventory service. */
	private final InventoryService inventoryService;
	
	/** The document storage service. */
	private final DocumentStorageService documentStorageService;
	
	/** The fund accounting service. */
	private final FundAccountingService fundAccountingService;
	// Add FileImportService, FileExportService if they become part of
	// ServiceContainer
	// For now, assuming they are not managed singletons in ServiceContainer.
	
	/**
	 * Constructs an ApplicationContextImpl.
	 *
	 * @param primaryStage The primary stage of the application.
	 * @param menuBar The menu bar of the application.
	 * @param reportService The report service.
	 * @param reportConfigurationService The report configuration service.
	 * @param inventoryService The inventory service.
	 * @param documentStorageService The document storage service.
	 * @param fundAccountingService The fund accounting service.
	 * @throws IllegalArgumentException if primaryStage or menuBar is null.
	 */
	public ApplicationContextImpl(Stage primaryStage,
		MenuBar menuBar,
		ReportService reportService,
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
		this.reportConfigurationService = reportConfigurationService;
		this.inventoryService = inventoryService;
		this.documentStorageService = documentStorageService;
		this.fundAccountingService = fundAccountingService;
	}
	
	/**
	 * Constructs an ApplicationContextImpl with default instances for services and UI components.
	 *
	 * @param primaryStage The primary stage of the application.
	 * @param reportService The report service.
	 * @param reportConfigurationService The report configuration service.
	 * @param inventoryService The inventory service.
	 * @param documentStorageService The document storage service.
	 * @param fundAccountingService The fund accounting service.
	 */
	public ApplicationContextImpl(Stage primaryStage, // Renamed parameter for clarity
		ReportService reportService, // Renamed parameter for clarity
		ReportConfigurationService reportConfigurationService, // Renamed parameter for clarity
		InventoryService inventoryService, // Renamed parameter for clarity
		DocumentStorageService documentStorageService, // Renamed parameter for clarity
		FundAccountingService fundAccountingService) // Renamed parameter for clarity
	{
		this.primaryStage = primaryStage != null ? primaryStage : new Stage(); // Use provided or new Stage
		this.menuBar = new MenuBar(); // Always create a new MenuBar for this constructor
		this.reportService = reportService != null ? reportService : new ReportService();
		this.reportConfigurationService = reportConfigurationService != null ? reportConfigurationService : new ReportConfigurationService();
		this.inventoryService = inventoryService != null ? inventoryService : new InventoryService();
		this.documentStorageService = documentStorageService != null ? documentStorageService : new DocumentStorageService();
		this.fundAccountingService = fundAccountingService != null ? fundAccountingService : new FundAccountingService();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public Stage getPrimaryStage()
	{
		return this.primaryStage;
	}
	
	// @Override
	// public MenuBar getMenuBar() { // This comment block can be removed
	// return this.menuBar;
	// }
	
	/**
	 * {@inheritDoc}
	 */
	@Override public Company getCurrentCompany()
	{
		return CurrentCompany.getCompany();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public ReportService getReportService()
	{
		return this.reportService;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override public ReportConfigurationService getReportConfigurationService()
	{
		return this.reportConfigurationService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public InventoryService getInventoryService()
	{
		return this.inventoryService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public DocumentStorageService getDocumentStorageService()
	{
		return this.documentStorageService;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public FundAccountingService getFundAccountingService()
	{
		return this.fundAccountingService;
	}
	
	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Override @see nonprofitbookkeeping.core.ApplicationContext#getBudgetService()
	 *
	 * @return the budget service
	 */
	@Override
	public BudgetLine getBudgetService()
	{
		// TODO Auto-generated method stub
		return null;
		
	}


	
}
