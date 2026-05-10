
package nonprofitbookkeeping.ui.panels;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.service.DemoCompanySeeder;
import nonprofitbookkeeping.ui.UiSpacing;
import nonprofitbookkeeping.ui.actions.CreateOrEditCompanyActionFX;
import nonprofitbookkeeping.ui.helpers.AlertBox;


/**
 * Lists the companies stored inside the shared database and allows the user to preview
 * and open them. The legacy file-based workflow has been replaced with database-backed
 * persistence so the UI now operates on {@link CompanyRepository.CompanyRecord} entries.
 */
public class CompanySelectionPanelFX extends BorderPane
{
	
	/** The Constant UPDATED_FORMATTER. */
	private static final DateTimeFormatter UPDATED_FORMATTER =
		DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
			.withZone(ZoneId.systemDefault());
	
	/** The Constant SUCCESS_STATUS_STYLE. */
	private static final String SUCCESS_STATUS_STYLE = "-fx-text-fill: #1b5e20;";
	
	/** The Constant ERROR_STATUS_STYLE. */
	private static final String ERROR_STATUS_STYLE = "-fx-text-fill: #b00020;";
	
	/** Callback invoked when a company has been successfully opened. */
	@FunctionalInterface
	public interface OnCompanyOpenedHandler
	{
		
		/**
		 * On company opened.
		 *
		 * @param company the company
		 */
		void onCompanyOpened(Company company);
		
	}
	
	/** The repository. */
	private final CompanyRepository repository = new CompanyRepository();
	
	/** The company list. */
	private final ListView<CompanyRecord> companyList = new ListView<>();
	
	/** The company items. */
	private final ObservableList<CompanyRecord> companyItems =
		FXCollections.observableArrayList();
	
	/** The preview area. */
	private final TextArea previewArea = new TextArea();
	
	/** The status label. */
	private final Label statusLabel = new Label("No company opened.");
	
	/** The demo company seeder. */
	private final DemoCompanySeeder demoCompanySeeder = new DemoCompanySeeder();
	
	/** The company opened handler. */
	private OnCompanyOpenedHandler companyOpenedHandler;
	
	/** The error handler. */
	private Consumer<String> errorHandler =
		msg -> AlertBox.showError(null, msg);
	
	/**
	 * Instantiates a new company selection panel FX.
	 */
	public CompanySelectionPanelFX()
	{
		setPadding(UiSpacing.pageInsets());
		buildUI();
		reloadCompanyList();
		
	}
	
	/**
	 * Instantiates a new company selection panel FX.
	 *
	 * @param companyOpenedHandler the company opened handler
	 */
	public CompanySelectionPanelFX(OnCompanyOpenedHandler companyOpenedHandler)
	{
		this();
		this.companyOpenedHandler = companyOpenedHandler;
		
	}
	
	/**
	 * Allows callers to override how error messages are surfaced.
	 *
	 * @param handler the new on error
	 */
	public void setOnError(Consumer<String> handler)
	{
		
		if (handler != null)
		{
			this.errorHandler = handler;
		}
		
	}
	
	/**
	 * Sets the handler that will be notified when the user opens a company.
	 *
	 * @param handler the new on company opened handler
	 */
	public void setOnCompanyOpenedHandler(OnCompanyOpenedHandler handler)
	{
		this.companyOpenedHandler = handler;
		
	}
	
	/**
	 * Builds the UI.
	 */
	private void buildUI()
	{
		this.companyList.setItems(this.companyItems);
		this.companyList.setCellFactory(list -> new ListCell<>()
		{
			@Override
			protected void updateItem(CompanyRecord record, boolean empty)
			{
				super.updateItem(record, empty);
				
				if (empty || record == null)
				{
					setText(null);
				}
				else
				{
					String updatedText =
						record.updatedAt() == null ? "Unknown" :
							UPDATED_FORMATTER.format(record.updatedAt());
					setText(String.format("%s (ID: %d) — Updated %s",
						record.name(), record.id(), updatedText));
				}
				
			}
			
		});
		this.companyList.getSelectionModel().selectedItemProperty()
			.addListener((obs, oldVal, newVal) -> showPreview(newVal));
		
		this.previewArea.setEditable(false);
		this.previewArea.setWrapText(true);
		
		SplitPane splitPane = new SplitPane(this.companyList, this.previewArea);
		splitPane.setDividerPositions(0.4);
		setCenter(splitPane);
		
		Button openBtn = new Button("Open Selected");
		Button createBtn = new Button("Create New Company…");
		Button demoBtn = new Button("Create Demo Company");
		Button deleteBtn = new Button("Delete Selected");
		openBtn.setOnAction(e -> openSelected());
		createBtn.setOnAction(e -> createNew());
		demoBtn.setOnAction(e -> createDemoCompany());
		deleteBtn.setOnAction(e -> deleteSelected());
		
		this.statusLabel.getStyleClass().add("muted-text");
		HBox buttons = new HBox(UiSpacing.SECTION_SPACING, openBtn, createBtn, demoBtn, deleteBtn);
		buttons.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		HBox.setHgrow(openBtn, Priority.NEVER);
		HBox.setHgrow(createBtn, Priority.NEVER);
		BorderPane footer = new BorderPane();
		footer.setTop(this.statusLabel);
		BorderPane.setMargin(this.statusLabel, new Insets(0, UiSpacing.SECTION_SPACING, UiSpacing.SECTION_SPACING, UiSpacing.SECTION_SPACING));
		footer.setCenter(buttons);
		setBottom(footer);
		
	}
	
	/**
	 * Reload company list.
	 */
	private void reloadCompanyList()
	{
		this.companyItems.clear();
		
		if (!Database.isInitialized())
		{
			this.previewArea
				.setText("Initialize the H2 database to manage companies.");
			return;
		}
		
		try
		{
			this.companyItems.addAll(this.repository.listCompanies());
		}
		catch (SQLException e)
		{
			this.errorHandler
				.accept("Failed to load companies: " + e.getMessage());
		}
		
		if (!this.companyItems.isEmpty())
		{
			this.companyList.getSelectionModel().selectFirst();
		}
		else
		{
			this.previewArea.setText("No companies available.");
		}
		
	}
	
	/** Public hook allowing the surrounding UI to refresh the listing. */
	public void refreshCompanyList()
	{
		reloadCompanyList();
		
	}
	
	/**
	 * Select company.
	 *
	 * @param companyId the company id
	 */
	private void selectCompany(long companyId)
	{
		
		for (CompanyRecord record : this.companyItems)
		{
			
			if (record != null && record.id() == companyId)
			{
				this.companyList.getSelectionModel().select(record);
				return;
			}
			
		}
		
	}
	
	/**
	 * Show preview.
	 *
	 * @param record the record
	 */
	private void showPreview(CompanyRecord record)
	{
		
		if (record == null)
		{
			this.previewArea.clear();
			return;
		}
		
		try
		{
			Company company = this.repository.load(record.id());
			StringBuilder sb = new StringBuilder();
			
			if (company.getCompanyProfileModel() != null)
			{
				sb.append("Name: ")
					.append(nullToEmpty(
						company.getCompanyProfileModel().getCompanyName()))
					.append('\n');
				sb.append("Base currency: ")
					.append(nullToEmpty(
						company.getCompanyProfileModel().getBaseCurrency()))
					.append('\n');
				sb.append("Fiscal year start: ")
					.append(nullToEmpty(
						company.getCompanyProfileModel().getFiscalYearStart()))
					.append('\n');
				sb.append("Default bank account: ")
					.append(nullToEmpty(company.getCompanyProfileModel()
						.getDefaultBankAccount()))
					.append('\n');
			}
			
			sb.append("Accounts: ")
				.append(company.getChartOfAccounts() == null ? 0 :
					company.getChartOfAccounts().getAccounts().size())
				.append('\n');
			sb.append("Transactions: ")
				.append(company.getLedger() == null ||
					company.getLedger().getJournal() == null ? 0 :
						company.getLedger().getJournal()
							.getJournalTransactions().size());
			
			this.previewArea.setText(sb.toString());
		}
		catch (IOException | SQLException e)
		{
			this.previewArea
				.setText("Unable to preview company: " + e.getMessage());
		}
		
	}
	
	/**
	 * Open selected.
	 */
	void openSelected()
	{
		
		if (!Database.isInitialized())
		{
			setStatus("Initialize the database before opening a company.", true);
			this.errorHandler
				.accept("Initialize the database before opening a company.");
			return;
		}
		
		CompanyRecord record =
			this.companyList.getSelectionModel().getSelectedItem();
		
		if (record == null)
		{
			setStatus("No company selected.", true);
			this.errorHandler.accept("No company selected.");
			return;
		}
		
		try
		{
			CurrentCompany.loadFromPersistent(record.id());
			PreferencesService.setLastUsedCompanyId(record.id());
			
			Company openedCompany = CurrentCompany.getCompany();
			setStatus("Opened company: " + record.name(), false);
			
			if (this.companyOpenedHandler != null)
			{
				this.companyOpenedHandler
					.onCompanyOpened(openedCompany);
			}
			
		}
		catch (IOException e)
		{
			setStatus("Failed to open company: " + e.getMessage(), true);
			this.errorHandler
				.accept("Failed to open company: " + e.getMessage());
		}
		
	}
	
	/**
	 * Creates the new.
	 */
	private void createNew()
	{
		
		if (!Database.isInitialized())
		{
			setStatus("Initialize the database before creating a company.", true);
			this.errorHandler
				.accept("Initialize the database before creating a company.");
			return;
		}
		
		Stage owner =
			getScene() != null ? (Stage) getScene().getWindow() : null;
		new CreateOrEditCompanyActionFX(owner);
		reloadCompanyList();
		
		if (this.companyOpenedHandler != null &&
			CurrentCompany.getCompany() != null)
		{
			setStatus("Created and opened company.", false);
			this.companyOpenedHandler
				.onCompanyOpened(CurrentCompany.getCompany());
		}
		
	}
	
	/**
	 * Creates the demo company.
	 */
	private void createDemoCompany()
	{
		
		if (!Database.isInitialized())
		{
			setStatus("Initialize the database before creating a demo company.", true);
			this.errorHandler.accept(
				"Initialize the database before creating a demo company.");
			return;
		}
		
		Company demo = new Company();
		this.demoCompanySeeder.seed(demo);
		
		try
		{
			long id = this.repository.save(null, demo);
			CurrentCompany.forceCompanyLoad(id, demo);
			PreferencesService.setLastUsedCompanyId(id);
			reloadCompanyList();
			selectCompany(id);
			
			setStatus("Created and opened demo company.", false);
			if (this.companyOpenedHandler != null)
			{
				this.companyOpenedHandler.onCompanyOpened(demo);
			}
			
		}
		catch (IOException | SQLException ex)
		{
			setStatus("Failed to create demo company: " + ex.getMessage(), true);
			this.errorHandler
				.accept("Failed to create demo company: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Sets the status message text with success/error styling.
	 *
	 * @param message the message
	 * @param error whether this is an error status
	 */
	private void setStatus(String message, boolean error)
	{
		this.statusLabel.setText(message == null ? "" : message);
		this.statusLabel
			.setStyle(error ? ERROR_STATUS_STYLE : SUCCESS_STATUS_STYLE);
	}

	/**
	 * Delete selected.
	 */
	private void deleteSelected()
	{
		
		if (!Database.isInitialized())
		{
			setStatus("Initialize the database before deleting companies.", true);
			this.errorHandler
				.accept("Initialize the database before deleting companies.");
			return;
		}
		
		CompanyRecord record =
			this.companyList.getSelectionModel().getSelectedItem();
		
		if (record == null)
		{
			setStatus("Select a company to delete.", true);
			this.errorHandler.accept("Select a company to delete.");
			return;
		}
		
		Stage owner =
			getScene() != null ? (Stage) getScene().getWindow() : null;
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
			"Delete '" + record.name() + "'?", ButtonType.OK,
			ButtonType.CANCEL);
		
		if (owner != null)
		{
			confirm.initOwner(owner);
		}
		
		Optional<ButtonType> result = confirm.showAndWait();
		
		if (result.isEmpty() || result.get() != ButtonType.OK)
		{
			setStatus("Delete cancelled.", false);
			return;
		}
		
		try
		{
			this.repository.delete(record.id());
			
			if (CurrentCompany.getCurrentCompanyId() != null &&
				CurrentCompany.getCurrentCompanyId().equals(record.id()))
			{
				CurrentCompany.close();
			}
			
			reloadCompanyList();
			setStatus("Deleted company: " + record.name(), false);
		}
		catch (SQLException ex)
		{
			setStatus("Failed to delete company: " + ex.getMessage(), true);
			this.errorHandler
				.accept("Failed to delete company: " + ex.getMessage());
		}
		
	}
	
	/**
	 * Null to empty.
	 *
	 * @param value the value
	 * @return the string
	 */
	private static String nullToEmpty(String value)
	{
		return value == null ? "" : value;
		
	}
	
}
