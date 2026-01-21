
package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.util.FormatUtils;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.GeneralJournalEntryPanelFX;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.Journal;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JavaFX panel that displays journal entries from the current company's ledger.
 * It provides a table view ({@link #journalDisplayTable}) for individual debit/credit entries
 * derived from {@link AccountingTransaction}s.
 * Includes filter controls for searching by description/account and date and
 * a built-in workspace for creating or editing transactions without leaving
 * the journal view. The panel listens for changes in the
 * {@link CurrentCompany} to reload data.
 */
public class SkeletonJournalPanel extends BorderPane
{
	private static final Logger LOGGER =
		LoggerFactory.getLogger(SkeletonJournalPanel.class);
	private static final String PLACEHOLDER = "—";
	
	/** TableView to display journal entries, using {@link JournalDisplayEntry} as the row model. */
	private TableView<JournalDisplayEntry> journalDisplayTable;
	/** ObservableList that backs the {@link #journalDisplayTable}, containing {@link JournalDisplayEntry} objects. */
	private ObservableList<JournalDisplayEntry> journalDataList;
	/** Listener to react to changes in the {@link CurrentCompany}, triggering a reload of journal data. */
	private CompanyChangeListener companyChangeListener;
	
	/** TextField for entering search terms to filter journal entries by description or account. */
	private TextField searchFilterField;
	/** Start date picker for filtering journal entries. */
	private DatePicker startDatePicker;
	/** End date picker for filtering journal entries. */
	private DatePicker endDatePicker;
	/** Button to apply the filters entered in {@link #searchFilterField} and the date range. */
	private Button applyFilterButton;
	/** Button to refresh the table without changing filters. */
	private Button refreshButton;
	/** Button that clears all filter criteria. */
	private Button clearFilterButton;
	/** Button to initiate creating a new journal entry from the in-panel workspace. */
	private Button createTransactionButton;
	/** Button to move the currently selected entry into the workspace for editing. */
	private Button editSelectedButton;
	/** Button to close the workspace and return to the entry preview state. */
	private Button closeEditorButton;
	/** Button to delete the selected journal entry's original transaction. */
	private Button deleteEntryButton;
	
	/** HBox container for the filter input controls. */
	private HBox filterControlsBox;
	/** ScrollPane to ensure filter controls are accessible if they overflow. */
	private ScrollPane filterScrollPane;
	/** Pane hosting either the entry preview or the editor workspace. */
	private StackPane editorHost;
	/** Preview container displaying a summary of the currently selected entry. */
	private VBox previewContainer;
	/** Label summarising the workspace mode (preview vs. editing). */
	private Label editorModeLabel;
	private Label previewInstructionLabel;
	private Label previewDateLabel;
	private Label previewTransactionLabel;
	private Label previewAccountLabel;
	private Label previewDescriptionLabel;
	private Label previewCounterpartyLabel;
	private Label previewAmountLabel;
	private Label previewFundLabel;
	private boolean editorActive;
	
	/**
	 * Constructs a new {@code SkeletonJournalPanel}.
	 * Initializes the UI layout, including filter controls at the top,
	 * the main table for journal entries in the center, and action buttons at the bottom.
	 * Sets up table columns, event listeners, and performs an initial data load.
	 */
	public SkeletonJournalPanel()
	{
		setPadding(new Insets(15)); // Overall padding
		
		// Initialize collections
		this.journalDataList = FXCollections.observableArrayList();
		this.journalDisplayTable = new TableView<>(this.journalDataList);
		this.journalDisplayTable.getSelectionModel()
			.setSelectionMode(SelectionMode.MULTIPLE);
		this.journalDisplayTable
			.setPlaceholder(new Label(
				"No journal entries to display or company not open."));
		this.journalDisplayTable.setRowFactory(tv -> {
			TableRow<JournalDisplayEntry> row = new TableRow<>();
			
			row.setOnMouseClicked(event -> {
				
				if (event.getClickCount() == 2 && !row.isEmpty())
				{
					JournalDisplayEntry entry = row.getItem();
					
					if (entry != null)
					{
						openEditor(entry.getOriginalTransaction());
					}
					
				}
				
			});
			
			return row;
		});
		
		KeyCodeCombination copyCombination = new KeyCodeCombination(KeyCode.C,
			KeyCombination.CONTROL_DOWN);
		this.journalDisplayTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			
			if (copyCombination.match(event))
			{
				JournalDisplayEntry entry =
					this.journalDisplayTable.getSelectionModel()
						.getSelectedItem();
				
				if (entry != null)
				{
					ClipboardContent content = new ClipboardContent();
					content.putString(buildClipboardSummary(entry));
					Clipboard.getSystemClipboard().setContent(content);
				}
				
				event.consume();
			}
			
		});
		
		// Filter Controls (Top)
		this.filterControlsBox = new HBox();
		this.filterControlsBox.setPadding(new Insets(0, 0, 10, 0));
		this.filterControlsBox.setSpacing(10);
		this.filterControlsBox.setAlignment(Pos.CENTER_LEFT);
		KeyCodeCombination findCombination = new KeyCodeCombination(KeyCode.F,
			KeyCombination.CONTROL_DOWN);
		KeyCodeCombination undoCombination = new KeyCodeCombination(KeyCode.Z,
			KeyCombination.CONTROL_DOWN);
		this.filterControlsBox.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			
			if (findCombination.match(event))
			{
				this.searchFilterField.requestFocus();
				event.consume();
			}
			else if (undoCombination.match(event))
			{
				clearFilters();
				event.consume();
			}
			
		});
		
		// Filter
		Label filterLabel = new Label("Filter:");
		// search
		this.searchFilterField = new TextField();
		this.searchFilterField.setPromptText("Search description/account...");
		this.searchFilterField.setPrefWidth(200);
		this.searchFilterField
			.setTooltip(new Tooltip("Search by description or account name."));
		// date range
		this.startDatePicker = new DatePicker();
		this.startDatePicker.setPromptText("Start Date");
		this.endDatePicker = new DatePicker();
		this.endDatePicker.setPromptText("End Date");
		// apply
		this.applyFilterButton = new Button("Apply Filter");
		this.refreshButton = new Button("Refresh");
		this.clearFilterButton = new Button("Clear Filter");
		this.applyFilterButton.setTooltip(
			new Tooltip("Apply the current search text and date range."));
		this.refreshButton
			.setTooltip(new Tooltip("Reload data using the existing filter."));
		this.clearFilterButton.setTooltip(
			new Tooltip("Remove all filters and show every entry."));
		this.filterControlsBox.getChildren().addAll(filterLabel,
			this.searchFilterField,
			this.startDatePicker, this.endDatePicker, this.applyFilterButton,
			this.refreshButton, this.clearFilterButton);
		
		// scroll pane
		this.filterScrollPane = new ScrollPane(this.filterControlsBox);
		this.filterScrollPane.setFitToWidth(true);
		this.filterScrollPane.setFitToHeight(true);
		this.filterScrollPane
			.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.filterScrollPane
			.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.setTop(this.filterScrollPane);
		
		// Workspace and action controls
		this.createTransactionButton = new Button("Create Transaction");
		this.editSelectedButton = new Button("Edit Selected");
		this.closeEditorButton = new Button("Close Workspace");
		this.deleteEntryButton = new Button("Delete Entry");
		this.closeEditorButton.setDisable(true);
		this.closeEditorButton.setVisible(false);
		this.closeEditorButton.setManaged(false);
		
		this.editSelectedButton.disableProperty().bind(
			Bindings.isNull(this.journalDisplayTable.getSelectionModel()
				.selectedItemProperty()));
		this.deleteEntryButton.disableProperty()
			.bind(Bindings.isEmpty(this.journalDisplayTable.getSelectionModel()
				.getSelectedItems()));
		
		this.editorModeLabel = new Label("Entry preview");
		this.editorModeLabel.getStyleClass().add("journal-editor-mode-label");
		
		this.previewContainer = new VBox(10);
		this.previewContainer.setAlignment(Pos.TOP_LEFT);
		this.previewContainer.setPadding(new Insets(10));
		
		Label previewTitle = new Label("Entry Preview");
		previewTitle.getStyleClass().add("journal-preview-title");
		
		this.previewInstructionLabel = new Label();
		this.previewInstructionLabel.setWrapText(true);
		
		this.previewDateLabel = new Label();
		this.previewTransactionLabel = new Label();
		this.previewAccountLabel = new Label();
		this.previewDescriptionLabel = new Label();
		this.previewCounterpartyLabel = new Label();
		this.previewAmountLabel = new Label();
		this.previewFundLabel = new Label();
		
		this.previewTransactionLabel.setWrapText(true);
		this.previewAccountLabel.setWrapText(true);
		this.previewDescriptionLabel.setWrapText(true);
		this.previewCounterpartyLabel.setWrapText(true);
		this.previewAmountLabel.setWrapText(true);
		this.previewFundLabel.setWrapText(true);
		
		Separator previewDivider = new Separator();
		previewDivider.setMaxWidth(Double.MAX_VALUE);
		
		this.previewContainer.getChildren().addAll(previewTitle,
			this.previewInstructionLabel,
			previewDivider, this.previewDateLabel, this.previewTransactionLabel,
			this.previewAccountLabel, this.previewDescriptionLabel,
			this.previewCounterpartyLabel,
			this.previewAmountLabel, this.previewFundLabel);
		
		this.editorHost = new StackPane();
		this.editorHost.setPadding(new Insets(10));
		this.editorHost.getChildren().add(this.previewContainer);
		
		this.journalDisplayTable.getSelectionModel().selectedItemProperty()
			.addListener((obs, oldSelection,
				newSelection) -> updatePreview(newSelection));
		
		ToolBar editorToolbar =
			new ToolBar(this.createTransactionButton, this.editSelectedButton,
				new Separator(), this.closeEditorButton);
		editorToolbar.setPadding(new Insets(0, 0, 0, 0));
		
		VBox editorHeader = new VBox(6);
		Label workspaceTitle = new Label("Transaction Workspace");
		workspaceTitle.getStyleClass().add("journal-editor-title");
		editorHeader.getChildren().addAll(workspaceTitle, editorToolbar,
			this.editorModeLabel);
		editorHeader.setPadding(new Insets(10, 10, 10, 10));
		
		BorderPane editorPane = new BorderPane();
		editorPane.setTop(editorHeader);
		editorPane.setCenter(this.editorHost);
		
		ToolBar tableActionsToolbar = new ToolBar(this.deleteEntryButton);
		tableActionsToolbar.setPadding(new Insets(10, 0, 0, 0));
		
		BorderPane tablePane = new BorderPane();
		tablePane.setCenter(this.journalDisplayTable);
		tablePane.setBottom(tableActionsToolbar);
		
		SplitPane contentSplitPane = new SplitPane(tablePane, editorPane);
		contentSplitPane.setDividerPositions(0.62);
		
		// Setup and initial load
		setupTableColumns();
		setCenter(contentSplitPane);
		
		resetEditorWorkspace();
		setupEventListenersAndRefresh();
		
	}
	
	/**
	 * Sets up the columns for the {@link #journalDisplayTable}.
	 * Defines columns for Date, Transaction ID, Account, Description, Debit, and Credit.
	 * Cell value factories are configured using {@link PropertyValueFactory} to bind to
	 * properties of the {@link JournalDisplayEntry} class.
	 */
	@SuppressWarnings("unchecked")
	private void setupTableColumns()
	{
		this.journalDisplayTable.getColumns().clear();
		
		TableColumn<JournalDisplayEntry, String> dateCol =
			new TableColumn<>("Date");
		dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
		dateCol.setPrefWidth(90);
		
		TableColumn<JournalDisplayEntry, String> transIdCol =
			new TableColumn<>("Transaction ID");
		transIdCol
			.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
		transIdCol.setPrefWidth(120);
		
		TableColumn<JournalDisplayEntry, String> accountCol =
			new TableColumn<>("Account");
		accountCol
			.setCellValueFactory(new PropertyValueFactory<>("accountName"));
		accountCol.setPrefWidth(150);
		
		TableColumn<JournalDisplayEntry, String> descCol =
			new TableColumn<>("Description");
		descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
		descCol.setPrefWidth(220);
		
		TableColumn<JournalDisplayEntry, String> toFromCol =
			new TableColumn<>("To/From");
		toFromCol.setCellValueFactory(new PropertyValueFactory<>("toFrom"));
		toFromCol.setPrefWidth(120);
		
		TableColumn<JournalDisplayEntry, String> checkCol =
			new TableColumn<>("Check #");
		checkCol.setCellValueFactory(new PropertyValueFactory<>("checkNumber"));
		checkCol.setPrefWidth(80);
		
		TableColumn<JournalDisplayEntry, String> clearBankCol =
			new TableColumn<>("Clear Bank");
		clearBankCol
			.setCellValueFactory(new PropertyValueFactory<>("clearBank"));
		clearBankCol.setPrefWidth(100);
		
		TableColumn<JournalDisplayEntry, String> budgetCol =
			new TableColumn<>("Budget Tracking");
		budgetCol
			.setCellValueFactory(new PropertyValueFactory<>("budgetTracking"));
		budgetCol.setPrefWidth(120);
		
		TableColumn<JournalDisplayEntry, String> fundNameCol =
			new TableColumn<>("Fund Name");
		fundNameCol.setCellValueFactory(new PropertyValueFactory<>("fundName"));
		fundNameCol.setPrefWidth(120);
		
		TableColumn<JournalDisplayEntry, String> fundNumCol =
			new TableColumn<>("Fund #");
		fundNumCol
			.setCellValueFactory(new PropertyValueFactory<>("fundNumber"));
		fundNumCol.setPrefWidth(80);
		
		TableColumn<JournalDisplayEntry, String> debitCol =
			new TableColumn<>("Debit");
		debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
		debitCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		debitCol.setPrefWidth(90);
		
		TableColumn<JournalDisplayEntry, String> creditCol =
			new TableColumn<>("Credit");
		creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
		creditCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		creditCol.setPrefWidth(90);
		
		this.journalDisplayTable.getColumns().addAll(dateCol, transIdCol,
			accountCol, descCol,
			toFromCol, checkCol, clearBankCol, budgetCol, fundNameCol,
			fundNumCol,
			debitCol, creditCol);
		
	}
	
	/**
	 * Loads journal entry data for the {@link CurrentCompany} and populates the {@link #journalDisplayTable}.
	 * It clears any existing items in the table. If a company is open and its journal is available,
	 * it iterates through each {@link AccountingTransaction} and then through each {@link AccountingEntry}
	 * within that transaction, creating a {@link JournalDisplayEntry} for each.
	 * These display entries are added to {@link #journalDataList}, which updates the table.
	 * Transactions are typically displayed in reverse chronological order (newest first).
	 * If no company is open or no entries are found, a placeholder message is shown in the table.
	 */
	
	private void loadData()
	{
		this.journalDataList.clear();
		this.journalDisplayTable.setItems(this.journalDataList);
		
		if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
		{
			this.journalDisplayTable
				.setPlaceholder(
					new Label("No journal entries found or company not open."));
			return;
		}
		
		Company company = CurrentCompany.getCompany();
		
		if (company != null && company.getLedger() != null &&
			company.getLedger().getJournal() != null)
		{
			Journal journal = company.getLedger().getJournal();
			// Iterate in reverse to show newest transactions first at the top
			// of the list
			List<AccountingTransaction> transactions =
				journal.getJournalTransactions();
			
			for (int i = transactions.size() - 1; i >= 0; i--)
			{
				AccountingTransaction tx = transactions.get(i);
				
				if (tx.getEntries() != null)
				{
					
					for (AccountingEntry entry : tx.getEntries())
					{
						this.journalDataList
							.add(new JournalDisplayEntry(tx, entry));
					}
					
				}
				
			}
			
		}
		
		if (this.journalDataList.isEmpty())
		{
			this.journalDisplayTable
				.setPlaceholder(
					new Label("No journal entries found or company not open."));
		}
		
		// No need for an 'else' to set placeholder to null, TableView handles
		// it.
		if (!this.editorActive)
		{
			updatePreview(
				this.journalDisplayTable.getSelectionModel().getSelectedItem());
		}
		
	}
	
	/**
	 * Sets up event listeners for UI components and performs an initial data refresh.
	 * This includes:
	 * <ul>
	 *   <li>Registering a {@link CompanyChangeListener} to reload journal data when the current company changes.</li>
	     *   <li>Setting action handlers for the filtering controls and the workspace buttons used to create, edit,
	     *       or delete transactions.</li>
	 *   <li>Performing an initial call to {@link #loadData()} to populate the table.</li>
	 * </ul>
	 */
	private void setupEventListenersAndRefresh()
	{
		// On company change
		this.companyChangeListener = new CompanyChangeListener()
		{
			@Override
			public void companyChange(boolean companyNowOpen)
			{
				loadData();
				resetEditorWorkspace();
				
			}
			
		};
		CurrentCompany.CompanyListener
			.addCompanyListener(this.companyChangeListener);
		
		// On filter
		this.applyFilterButton.setOnAction(e -> onFilterButtonAction());
		this.refreshButton.setOnAction(e -> refresh());
		this.clearFilterButton.setOnAction(e -> clearFilters());
		this.createTransactionButton.setOnAction(e -> openEditor(null));
		this.editSelectedButton.setOnAction(e -> onEditAction());
		this.closeEditorButton.setOnAction(e -> resetEditorWorkspace());
		this.deleteEntryButton.setOnAction(e -> onDeleteAction());
		
		loadData(); // Initial data load
		
	}
	
	/**
	 * On Filter Button
	 */
	void onFilterButtonAction()
	{
		String search = this.searchFilterField.getText().toLowerCase();
		LocalDate start = this.startDatePicker.getValue();
		LocalDate end = this.endDatePicker.getValue();
		
		loadData();
		
		if ((search == null || search.isBlank()) && start == null &&
			end == null)
		{
			return; // nothing to filter
		}
		
		DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
		ObservableList<JournalDisplayEntry> filtered =
			FXCollections.observableArrayList();
		
		for (JournalDisplayEntry entry : this.journalDataList)
		{
			boolean match = true;
			
			if (search != null && !search.isBlank())
			{
				match &= entry.descriptionProperty().get().toLowerCase()
					.contains(search) ||
					entry.accountNameProperty().get().toLowerCase()
						.contains(search);
			}
			
			if (start != null || end != null)
			{
				LocalDate entryDate =
					LocalDate.parse(entry.dateProperty().get(), fmt);
				
				if (start != null)
				{
					match &= !entryDate.isBefore(start);
				}
				
				if (end != null)
				{
					match &= !entryDate.isAfter(end);
				}
				
			}
			
			if (match)
			{
				filtered.add(entry);
			}
			
		}
		
		this.journalDisplayTable.setItems(filtered);
		
		JournalDisplayEntry selected =
			this.journalDisplayTable.getSelectionModel().getSelectedItem();
		
		if (selected != null && !filtered.contains(selected))
		{
			this.journalDisplayTable.getSelectionModel().clearSelection();
			selected = null;
		}
		
		if (!this.editorActive)
		{
			updatePreview(selected);
		}
		
	}
	
	/** Clears all filter controls and reloads the full journal. */
	void clearFilters()
	{
		this.searchFilterField.clear();
		this.startDatePicker.setValue(null);
		this.endDatePicker.setValue(null);
		loadData();
		this.journalDisplayTable.setItems(this.journalDataList);
		this.journalDisplayTable.getSelectionModel().clearSelection();
		
		if (!this.editorActive)
		{
			updatePreview(null);
		}
		
	}
	
	/**
	 * Reloads journal data using the current filter settings. This is used
	 * by the Refresh button to show newly added or edited entries without
	 * clearing the user's search or date filters.
	 */
	void refresh()
	{
		onFilterButtonAction();
		
	}
	

	/** Moves keyboard focus to the search filter field. */
	public void focusSearchField()
	{
		this.searchFilterField.requestFocus();
		this.searchFilterField.selectAll();
		
	}
	
	private void showPreview()
	{
		this.editorHost.getChildren().setAll(this.previewContainer);
		this.editorModeLabel.setText("Entry preview");
		this.closeEditorButton.setDisable(true);
		
	}
	
	private void resetEditorWorkspace()
	{
		this.editorActive = false;
		showPreview();
		updatePreview(
			this.journalDisplayTable.getSelectionModel().getSelectedItem());
		
	}
	
	private void updatePreview(JournalDisplayEntry entry)
	{
		
		if (this.previewInstructionLabel == null)
		{
			return;
		}
		
		if (entry == null)
		{
			this.previewInstructionLabel.setText(
				"Select a journal entry to see its details or choose \"Create Transaction\" to open the editor window.");
		}
		else
		{
			this.previewInstructionLabel
				.setText(
					"Use the buttons above to open this transaction in the editor window.");
		}
		
		this.previewDateLabel.setText(
			"Date: " + withPlaceholder(entry == null ? null : entry.getDate()));
		this.previewTransactionLabel
			.setText("Transaction ID: " + withPlaceholder(
				entry == null ? null : entry.getTransactionId()));
		this.previewAccountLabel
			.setText("Account: " +
				withPlaceholder(entry == null ? null : entry.getAccountName()));
		this.previewDescriptionLabel
			.setText("Memo: " +
				withPlaceholder(entry == null ? null : entry.getDescription()));
		this.previewCounterpartyLabel.setText(buildCounterpartyLine(entry));
		this.previewAmountLabel
			.setText("Amount: " +
				(entry == null ? PLACEHOLDER : summariseAmount(entry)));
		this.previewFundLabel.setText(buildFundLine(entry));
		
		if (!this.editorActive)
		{
			showPreview();
		}
		
	}
	
	private String withPlaceholder(String value)
	{
		return (value == null || value.isBlank()) ? PLACEHOLDER : value;
		
	}
	
	private String summariseAmount(JournalDisplayEntry entry)
	{
		BigDecimal debit = FormatUtils.parseCurrency(entry.getDebit());
		
		if (debit != null && debit.compareTo(BigDecimal.ZERO) != 0)
		{
			return "Debit " + entry.getDebit();
		}
		
		BigDecimal credit = FormatUtils.parseCurrency(entry.getCredit());
		
		if (credit != null && credit.compareTo(BigDecimal.ZERO) != 0)
		{
			return "Credit " + entry.getCredit();
		}
		
		return PLACEHOLDER;
		
	}
	
	private String buildCounterpartyLine(JournalDisplayEntry entry)
	{
		
		if (entry == null)
		{
			return "Counterparty: " + PLACEHOLDER + System.lineSeparator() +
				"Check #: " + PLACEHOLDER + "   Clear Bank: " + PLACEHOLDER;
		}
		
		String toFrom = withPlaceholder(entry.getToFrom());
		String check = withPlaceholder(entry.getCheckNumber());
		String clearBank = withPlaceholder(entry.getClearBank());
		
		return "Counterparty: " + toFrom + System.lineSeparator() +
			"Check #: " + check +
			"   Clear Bank: " + clearBank;
		
	}
	
	private String buildFundLine(JournalDisplayEntry entry)
	{
		
		if (entry == null)
		{
			return "Fund: " + PLACEHOLDER;
		}
		
		String fundName = withPlaceholder(entry.getFundName());
		String fundNumber = withPlaceholder(entry.getFundNumber());
		
		if (PLACEHOLDER.equals(fundName) && PLACEHOLDER.equals(fundNumber))
		{
			return "Fund: " + PLACEHOLDER;
		}
		
		StringBuilder builder = new StringBuilder("Fund: ");
		
		if (!PLACEHOLDER.equals(fundName))
		{
			builder.append(fundName);
		}
		
		if (!PLACEHOLDER.equals(fundNumber))
		{
			
			if (!PLACEHOLDER.equals(fundName))
			{
				builder.append("  •  #");
			}
			else
			{
				builder.append('#');
			}
			
			builder.append(fundNumber);
		}
		
		if (builder.length() == "Fund: ".length())
		{
			builder.append(PLACEHOLDER);
		}
		
		return builder.toString();
		
	}
	
	private String buildClipboardSummary(JournalDisplayEntry entry)
	{
		
		if (entry == null)
		{
			return "";
		}
		
		String amount = summariseAmount(entry);
		
		return String.join("\t",
			entry.dateProperty().get(),
			entry.transactionIdProperty().get(),
			entry.accountNameProperty().get(),
			entry.descriptionProperty().get(),
			amount == null ? "" : amount);
		
	}
	
	private void focusOnTransaction(long bookingTimestamp)
	{
		
		if (bookingTimestamp <= 0)
		{
			return;
		}
		
		for (JournalDisplayEntry entry : this.journalDisplayTable.getItems())
		{
			AccountingTransaction tx = entry.getOriginalTransaction();
			
			if (tx != null && tx.getBookingDateTimestamp() == bookingTimestamp)
			{
				this.journalDisplayTable.getSelectionModel().select(entry);
				
				if (!this.editorActive)
				{
					updatePreview(entry);
				}
				
				break;
			}
			
		}
		
	}
	
	private void handlePersistAndRefresh(AccountingTransaction tx)
	{
		boolean persisted = true;
		
		try
		{
			CurrentCompany.persist();
		}
		catch (Exception ex)
		{
			persisted = false;
			LOGGER.error("Unable to persist journal changes", ex);
			AlertBox.showError(
				getScene() == null ? null : getScene().getWindow(),
				"Unable to save the transaction. Please try again.");
		}
		
		if (!persisted)
		{
			return;
		}
		
		refresh();
		
		if (tx != null)
		{
			focusOnTransaction(tx.getBookingDateTimestamp());
		}
		
		resetEditorWorkspace();
		
	}
	
	/**
	 * On Edit Button
	 */
	void onEditAction()
	{
		JournalDisplayEntry selected =
			this.journalDisplayTable.getSelectionModel().getSelectedItem();
		
		if (selected != null)
		{
			AccountingTransaction originalTx =
				selected.getOriginalTransaction();
			openEditor(originalTx);
		}
		else
		{
			LOGGER.warn("No journal entry selected for editing.");
			AlertBox.showError(getScene().getWindow(), "No entry selected.");
		}
		
	}
	
	/**
	 * On delete button
	 */
	void onDeleteAction()
	{
		ObservableList<JournalDisplayEntry> selected =
			this.journalDisplayTable.getSelectionModel().getSelectedItems();
		
		if (selected == null || selected.isEmpty())
		{
			LOGGER.warn("No journal entry selected for deletion.");
			AlertBox.showError(getScene().getWindow(),
				"No entry selected for deletion.");
			return;
		}
		
		Company company = CurrentCompany.getCompany();
		
		if (company != null && company.getLedger() != null &&
			company.getLedger().getJournal() != null)
		{
			Journal journal = company.getLedger().getJournal();
			boolean anyDeleted = false;
			
			List<JournalDisplayEntry> toDelete = new ArrayList<>(selected);
			
			for (JournalDisplayEntry entry : toDelete)
			{
				AccountingTransaction originalTx =
					entry.getOriginalTransaction();
				anyDeleted |= journal
					.deleteTransaction(originalTx.getBookingDateTimestamp());
			}
			
			if (anyDeleted)
			{
				try
				{
					CurrentCompany.persist();
				}
				catch (IOException ex)
				{
					LOGGER.error("Unable to persist journal changes", ex);
					AlertBox.showError(
						getScene() == null ? null : getScene().getWindow(),
						"Unable to save deleted transactions. Please try again.");
					return;
				}
				
				loadData();
				resetEditorWorkspace();
				LOGGER.info("Deleted selected entries.");
			}
			else
			{
				LOGGER.error("Failed to delete selected entries.");
				AlertBox.showError(getScene().getWindow(), "Deletion failed.");
			}
			
		}
		
	}
	
	/** 
	 * Opens the GeneralJournalEntryPanelFX for creating or 
	 * editing a transaction. 
	 * */
	private void openEditor(AccountingTransaction existing)
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null ||
			company.getLedger().getJournal() == null)
		{
			AlertBox.showError(
				getScene() == null ? null : getScene().getWindow(),
				"No company open.");
			return;
		}
		
		Journal journal = company.getLedger().getJournal();
		Stage ownerStage =
			getScene() != null ? (Stage) getScene().getWindow() : null;
		Stage dialog = new Stage();
		
		if (ownerStage != null)
		{
			dialog.initOwner(ownerStage);
		}
		
		dialog.initModality(Modality.APPLICATION_MODAL);
		
		String modeDescription =
			(existing == null) ? "Create Journal Entry" : "Edit Journal Entry";
		dialog.setTitle(modeDescription);
		
		GeneralJournalEntryPanelFX editorPane;
		
		if (existing == null)
		{
			editorPane = new GeneralJournalEntryPanelFX(tx -> {
				journal.addTransaction(tx);
				handlePersistAndRefresh(tx);
				dialog.close();
			});
		}
		else
		{
			editorPane = new GeneralJournalEntryPanelFX(existing, tx -> {
				journal.updateTransaction(tx);
				handlePersistAndRefresh(tx);
				dialog.close();
			});
		}
		
		Scene scene = new Scene(editorPane, 900, 600);
		
		if (ownerStage != null && ownerStage.getScene() != null)
		{
			scene.getStylesheets()
				.addAll(ownerStage.getScene().getStylesheets());
		}
		
		dialog.setScene(scene);
		dialog.setResizable(true);
		dialog.showAndWait();
		
	}
	
	/**
	 * Represents a single displayable row in the journal table.
	 * Each {@code JournalDisplayEntry} corresponds to one {@link AccountingEntry}
	 * from an {@link AccountingTransaction}. It flattens the transaction data for table display,
	 * showing details like date, transaction ID, account name, description, and debit/credit amounts.
	 * It also holds a reference to the original {@link AccountingTransaction} for operations like editing or deleting.
	 */
	public static class JournalDisplayEntry
	{
		/** The date of the transaction. */
		private final SimpleStringProperty date;
		/** The unique ID of the transaction (typically the booking timestamp). */
		private final SimpleStringProperty transactionId;
		/** The name of the account associated with this specific journal entry line. */
		private final SimpleStringProperty accountName;
		/** The overall description or memo of the transaction. */
		private final SimpleStringProperty description;
		/** Payee or counterparty for this transaction. */
		private final SimpleStringProperty toFrom;
		/** Check number associated with the transaction. */
		private final SimpleStringProperty checkNumber;
		/** Clearing bank information. */
		private final SimpleStringProperty clearBank;
		/** Budget tracking notes. */
		private final SimpleStringProperty budgetTracking;
		/** Associated fund name for the transaction. */
		private final SimpleStringProperty fundName;
		/** Fund number for this entry line. */
		private final SimpleStringProperty fundNumber;
		/** The debit amount for this entry line, as a string. Empty if it's a credit. */
		private final SimpleStringProperty debit;
		/** The credit amount for this entry line, as a string. Empty if it's a debit. */
		private final SimpleStringProperty credit;
		/** A reference to the original {@link AccountingTransaction} this display entry belongs to. */
		private final AccountingTransaction originalTransaction;
		
		/**
		 * Constructs a new {@code JournalDisplayEntry}.
		 *
		 * @param tx The source {@link AccountingTransaction}. Must not be null.
		 * @param entry The specific {@link AccountingEntry} within the transaction to display. Must not be null.
		 *              The entry's account and amount details are used to populate debit/credit columns.
		 */
		public JournalDisplayEntry(AccountingTransaction tx,
			AccountingEntry entry)
		{
			this.originalTransaction = tx;
			this.date = new SimpleStringProperty(tx.getDate());
			this.transactionId =
				new SimpleStringProperty(
					String.valueOf(tx.getBookingDateTimestamp()));
			this.description =
				new SimpleStringProperty(tx.getDescription() != null ?
					tx.getDescription() :
					(tx.getMemo() != null ? tx.getMemo() : ""));
			this.toFrom = new SimpleStringProperty(
				tx.getToFrom() != null ? tx.getToFrom() : "");
			this.checkNumber = new SimpleStringProperty(
				tx.getCheckNumber() != null ?
					tx.getCheckNumber() : "");
			this.clearBank = new SimpleStringProperty(
				tx.getClearBank() != null ?
					tx.getClearBank() : "");
			this.budgetTracking = new SimpleStringProperty(
				tx.getBudgetTracking() != null ?
					tx.getBudgetTracking() : "");
			this.fundName = new SimpleStringProperty(
				tx.getAssociatedFundName() != null ?
					tx.getAssociatedFundName() : "");
			this.fundNumber = new SimpleStringProperty(
				entry != null &&
					entry.getFundNumber() != null ?
						entry.getFundNumber() : "");
			
			if (entry != null && entry.getAccount() != null)
			{
				this.accountName =
					new SimpleStringProperty(entry.getAccount().getName());
				BigDecimal amount = entry.getAmount() != null ?
					entry.getAmount() : BigDecimal.ZERO;
				
				if (entry.getAccountSide() == AccountSide.DEBIT)
				{
					this.debit = new SimpleStringProperty(
						FormatUtils.formatCurrency(amount));
					this.credit = new SimpleStringProperty("");
				}
				else
				{
					this.debit = new SimpleStringProperty("");
					this.credit = new SimpleStringProperty(
						FormatUtils.formatCurrency(amount));
				}
				
			}
			else
			{
				// Fallback, should ideally not occur with valid data
				this.accountName =
					new SimpleStringProperty("Error: No Account");
				this.debit = new SimpleStringProperty("");
				this.credit = new SimpleStringProperty("");
			}
			
		}
		
		/**
		 * Gets the JavaFX property for the transaction date.
		 * @return The date property.
		 */
		public StringProperty dateProperty()
		{
			return this.date;
			
		}
		
		/**
		 * Gets the JavaFX property for the transaction ID.
		 * @return The transaction ID property.
		 */
		public StringProperty transactionIdProperty()
		{
			return this.transactionId;
			
		}
		
		/**
		 * Gets the JavaFX property for the account name of this entry line.
		 * @return The account name property.
		 */
		public StringProperty accountNameProperty()
		{
			return this.accountName;
			
		}
		
		/**
		 * Gets the JavaFX property for the transaction description/memo.
		 * @return The description property.
		 */
		public StringProperty descriptionProperty()
		{
			return this.description;
			
		}
		
		/** Returns the to/from property. */
		public StringProperty toFromProperty()
		{
			return this.toFrom;
			
		}
		
		/** Returns the check number property. */
		public StringProperty checkNumberProperty()
		{
			return this.checkNumber;
			
		}
		
		/** Returns the clear bank property. */
		public StringProperty clearBankProperty()
		{
			return this.clearBank;
			
		}
		
		/** Returns the budget tracking property. */
		public StringProperty budgetTrackingProperty()
		{
			return this.budgetTracking;
			
		}
		
		/** Returns the fund name property. */
		public StringProperty fundNameProperty()
		{
			return this.fundName;
			
		}
		
		/** Returns the fund number property. */
		public StringProperty fundNumberProperty()
		{
			return this.fundNumber;
			
		}
		
		/**
		 * Gets the JavaFX property for the debit amount string.
		 * @return The debit amount property.
		 */
		public StringProperty debitProperty()
		{
			return this.debit;
			
		}
		
		/**
		 * Gets the JavaFX property for the credit amount string.
		 * @return The credit amount property.
		 */
		public StringProperty creditProperty()
		{
			return this.credit;
			
		}
		
		/**
		 * Gets the original {@link AccountingTransaction} from which this display entry was derived.
		 * This is useful for actions like editing or deleting the full transaction.
		 * @return The original {@link AccountingTransaction}.
		 */
		public AccountingTransaction getOriginalTransaction()
		{
			return this.originalTransaction;
			
		}
		
		/** 
		 * Gets the transaction date string. @return The date. 
		 * */
		public String getDate()
		{
			return this.date.get();
			
		}
		
		/** 
		 * Gets the transaction ID string. @return The transaction ID. 
		 * */
		public String getTransactionId()
		{
			return this.transactionId.get();
			
		}
		
		/** 
		 * Gets the account name string for this entry line. @return The account name. 
		 * */
		public String getAccountName()
		{
			return this.accountName.get();
			
		}
		
		/** 
		 * Gets the transaction description/memo string. @return The description. 
		 * */
		public String getDescription()
		{
			return this.description.get();
			
		}
		
		/** Returns the to/from value. */
		public String getToFrom()
		{
			return this.toFrom.get();
			
		}
		
		/** Returns the check number. */
		public String getCheckNumber()
		{
			return this.checkNumber.get();
			
		}
		
		/** Returns the clearing bank string. */
		public String getClearBank()
		{
			return this.clearBank.get();
			
		}
		
		/** Returns the budget tracking notes. */
		public String getBudgetTracking()
		{
			return this.budgetTracking.get();
			
		}
		
		/** Returns the fund name. */
		public String getFundName()
		{
			return this.fundName.get();
			
		}
		
		/** Returns the fund number. */
		public String getFundNumber()
		{
			return this.fundNumber.get();
			
		}
		
		/** 
		 * Gets the debit amount string for this entry line. 
		 * Empty if it's a credit. @return The debit amount. 
		 * */
		public String getDebit()
		{
			return this.debit.get();
			
		}
		
		/** 
		 * Gets the credit amount string for this entry line. 
		 * Empty if it's a debit. @return The credit amount. 
		 * */
		public String getCredit()
		{
			return this.credit.get();
			
		}
		
	}
	
}
