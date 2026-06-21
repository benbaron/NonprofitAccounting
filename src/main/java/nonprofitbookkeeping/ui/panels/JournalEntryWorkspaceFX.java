package nonprofitbookkeeping.ui.panels;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.Separator;
import javafx.util.Callback;
import javafx.util.StringConverter;


import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.DonorContact;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;
import nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase;
import nonprofitbookkeeping.service.DonorService;
import nonprofitbookkeeping.service.SettingsService;
import nonprofitbookkeeping.core.Database;
import java.io.IOException;
import java.util.EnumMap;
import nonprofitbookkeeping.ui.FundNameLookup;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.helpers.FocusCommitTextFieldTableCell;
import nonprofitbookkeeping.ui.javafx.supplemental.EntryRef;
import nonprofitbookkeeping.ui.javafx.supplemental.PersonRef;
import nonprofitbookkeeping.ui.javafx.supplemental.SupplementalLineRow;
import nonprofitbookkeeping.ui.javafx.supplemental.SupplementalLinesEditor;
import nonprofitbookkeeping.ui.javafx.supplemental.SupplementalLinesFxAdapter;
import nonprofitbookkeeping.ui.javafx.supplemental.SupplementalLinesTabs;
import nonprofitbookkeeping.util.FormatUtils;
import nonprofitbookkeeping.service.PersonService;
import nonprofitbookkeeping.model.Person;


/**
 * Shared workspace UI for creating or editing general journal entries.
 *
 * <p>The legacy code base exposed two very similar panels for "new" and
 * "edit" flows. This class re-imagines that experience with a single,
 * modernised workspace that emphasises a real-time balance summary and a
 * structured metadata editor. Both the old {@code GeneralJournalEntryPanelFX}
 * and {@code NewTransactionPanelFX} now delegate to this class.</p>
 */
public class JournalEntryWorkspaceFX extends BorderPane
{
	private static final String INFO_MODULE = "module";
	private static final String INFO_MODULE_DONATION = "DONATION";
	private static final String INFO_DONATION_ID = "donation_id";
	private static final String INFO_DONOR_ID = "donor_external_id";
	private static final String INFO_DONOR_NAME = "donor_name";

	/** CSS class used when account cell references an unknown account. */
	private static final String ACCOUNT_CELL_MISSING_CLASS =
		"account-cell-missing";

	/** Public row model so existing tests can reason about the table state. */
	public static class Line
	{
		
		/** The account. */
		public final StringProperty account = new SimpleStringProperty("");
		
		/** The debit. */
		public final ObjectProperty<BigDecimal> debit =
			new SimpleObjectProperty<>(BigDecimal.ZERO);
		
		/** The credit. */
		public final ObjectProperty<BigDecimal> credit =
			new SimpleObjectProperty<>(BigDecimal.ZERO);
		
	}
	
	/** The lines. */
	private final ObservableList<Line> lines =
		FXCollections.observableArrayList();
	
	/** The table. */
	private final TableView<Line> table = new TableView<>(this.lines);
	
	/** The date picker. */
	private final DatePicker datePicker = new DatePicker(LocalDate.now());
	
	/** The memo area. */
	private final TextArea memoArea = new TextArea();
	
	/** The to from field. */
	private final TextField toFromField = new TextField();
	
	/** The check number field. */
	private final TextField checkNumberField = new TextField();
	
	/** The clear bank field. */
	private final TextField clearBankField = new TextField();
	
	/** The bank field. */
	private final TextField bankField = new TextField();
	
	/** Reconciled toggle. */
	private final CheckBox reconciledCheckBox = new CheckBox("Reconciled");
	
	/** The budget tracking field. */
	private final TextField budgetTrackingField = new TextField();
	
	/** The fund names available from the active database. */
	private final ObservableList<String> fundNameChoices =
		FXCollections.observableArrayList();

	/** The associated fund name selector. */
	private final ComboBox<String> associatedFundNameField = new ComboBox<>();

	/** Enables donation metadata on this journal transaction. */
	private final CheckBox donationScheduleEnabled =
		new CheckBox("Donation schedule");

	/** Donation id attached to this journal transaction. */
	private final TextField donationIdField = new TextField();

	/** Donors available from the donor list. */
	private final ObservableList<DonorContact> donorChoices =
		FXCollections.observableArrayList();

	/** Donor id selector for the donation subschedule. */
	private final ComboBox<DonorContact> donorIdBox = new ComboBox<>();

	/** Donor name selector for the donation subschedule. */
	private final ComboBox<DonorContact> donorNameBox = new ComboBox<>();
	
	/** The debit total label. */
	private final Label debitTotalLabel = new Label();
	
	/** The credit total label. */
	private final Label creditTotalLabel = new Label();
	
	/** The difference label. */
	private final Label differenceLabel = new Label();
	
	/** The status badge. */
	private final Label statusBadge = new Label();
	
	/** The validation message. */
	private final Label validationMessage = new Label();
	
	/** The save button. */
	private final Button saveButton = new Button("Save");
	
	/** The add line button. */
	private final Button addLineButton = new Button("Add Line");
	
	/** The duplicate line button. */
	private final Button duplicateLineButton = new Button("Duplicate");
	
	/** The remove line button. */
	private final Button removeLineButton = new Button("Remove");
	
	/** The save error tooltip. */
	private final Tooltip saveErrorTooltip = new Tooltip();
	
	/** The on save. */
	private final Consumer<AccountingTransaction> onSave;
	
	/** The chart of accounts. */
	private final ChartOfAccounts chartOfAccounts;
	
	/** The original. */
	private AccountingTransaction original;
	
	/** The accounts by name. */
	private final Map<String, Account> accountsByName;
	
	/** The supplemental tabs. */
	private final SupplementalLinesTabs supplementalTabs =
		new SupplementalLinesTabs();
	
	/** Per-kind supplemental schedule selection checkboxes. */
	private final Map<SupplementalLineKind, CheckBox> supplementalSelections =
		new EnumMap<>(SupplementalLineKind.class);
	
	/** The heading text. */
	private final String headingText;
	
	/**
	 * Convenience constructor used when wiring inside dialogs.
	 *
	 * @param onSave the on save
	 */
	public JournalEntryWorkspaceFX(Consumer<AccountingTransaction> onSave)
	{
		this(null, onSave);
		
	}
	
	/** Convenience constructor for tests or logging only scenarios. */
	public JournalEntryWorkspaceFX()
	{
		this(tx -> {});
		
	}
	
	/**
	 * Creates a workspace for either a new entry (when {@code existing} is
	 * {@code null}) or for editing an existing transaction.
	 *
	 * @param existing the existing
	 * @param onSave the on save
	 */
	public JournalEntryWorkspaceFX(AccountingTransaction existing,
		Consumer<AccountingTransaction> onSave)
	{
		this(existing, onSave,
			existing == null ? "New Journal Entry" : "Edit Journal Entry");
		
	}
	
	/**
	 * Internal constructor allowing a custom heading.
	 *
	 * @param existing the existing
	 * @param onSave the on save
	 * @param headingText the heading text
	 */
	protected JournalEntryWorkspaceFX(AccountingTransaction existing,
		Consumer<AccountingTransaction> onSave, String headingText)
	{
		
		if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
		{
			throw new IllegalStateException(
				"JournalEntryWorkspaceFX requires an open company");
		}
		
		this.chartOfAccounts = resolveChartOfAccounts();
		this.accountsByName = buildAccountsByName(this.chartOfAccounts);
		configureFundSelector();
		configureDonationSchedule();
		this.onSave = onSave != null ? onSave : tx -> {};
		this.original = existing;
		this.headingText = headingText;
		
		initialiseUi();
		attachListeners();
		
		if (existing != null)
		{
			loadFromTransaction(existing);
		}
		else
		{
			addLine();
			applyDefaultAccountsFromSettings();
			updateSupplementalTabAvailability();
		}
		
		recalcTotals();
		markValidationPending();
		
	}
	
	/**
	 * Returns the backing list of lines. Primarily used by tests.
	 *
	 * @return the lines
	 */
	public ObservableList<Line> getLines()
	{
		return this.lines;
		
	}
	
	/**
	 * Returns the save button node for integration tests.
	 *
	 * @return the save button
	 */
	public Button getSaveButton()
	{
		return this.saveButton;
		
	}
	
	/**
	 * Exposes the add-line button primarily for UI tests.
	 *
	 * @return the adds the line button
	 */
	public Button getAddLineButton()
	{
		return this.addLineButton;
		
	}
	
	/**
	 * Exposes the remove-line button primarily for UI tests.
	 *
	 * @return the removes the line button
	 */
	public Button getRemoveLineButton()
	{
		return this.removeLineButton;
		
	}
	
	/**
	 * Exposes the duplicate-line button primarily for UI tests.
	 *
	 * @return the duplicate line button
	 */
	public Button getDuplicateLineButton()
	{
		return this.duplicateLineButton;
		
	}
	
	/**
	 * Returns the table of entry lines.
	 *
	 * @return the table
	 */
	public TableView<Line> getTable()
	{
		return this.table;
		
	}
	
	/** Builds the UI skeleton. */
	private void initialiseUi()
	{
		setPadding(new Insets(16));
		
		VBox root = new VBox(14);
		root.getStyleClass().add("journal-entry-workspace");

		Node topBar = buildTopBar();
		Node lineControls = buildLineControls();
		Node metadataArea = buildMetadataArea();
		Node supplementalPanel = buildSupplementalPanel();

		VBox topContent = new VBox(14, topBar, lineControls);
		topContent.setMinWidth(1120);
		ScrollPane topPane = scrollablePane(topContent, 230);

		configureTable();
		this.table.setMinWidth(1120);
		this.table.setMinHeight(360);
		this.table.setPrefHeight(520);
		ScrollPane tablePane = scrollablePane(this.table, 360);

		VBox bottomContent = new VBox(12, metadataArea, supplementalPanel);
		bottomContent.setMinWidth(1120);
		ScrollPane bottomPane = scrollablePane(bottomContent, 380);

		root.getChildren().addAll(topPane, tablePane, bottomPane);
		VBox.setVgrow(tablePane, Priority.ALWAYS);
		root.setMinWidth(100);
		root.setMinHeight(100);

		ScrollPane scrollPane = new ScrollPane(root);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(false);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		scrollPane.setPannable(true);
		setCenter(scrollPane);

		root.prefWidthProperty().bind(Bindings.createDoubleBinding(
			() -> Math.max(960, scrollPane.getViewportBounds().getWidth()),
			scrollPane.viewportBoundsProperty()));

		this.supplementalTabs.prefHeightProperty().bind(Bindings.max(180,
			bottomContent.heightProperty().multiply(0.40)));

		this.supplementalTabs.setPersonRefs(loadPersonRefs());
		
		this.table.setId("entryTable");
		this.saveButton.setId("saveBtn");
		this.datePicker.setId("datePicker");
		this.memoArea.setId("memoArea");
	}
	
	private ScrollPane scrollablePane(Node content, double preferredViewportHeight)
	{
		ScrollPane scrollPane = new ScrollPane(content);
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(false);
		scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		scrollPane.setPannable(true);
		scrollPane.setPrefViewportHeight(preferredViewportHeight);
		scrollPane.setMaxWidth(Double.MAX_VALUE);
		return scrollPane;
	}

	/**
	 * Builds the top bar.
	 *
	 * @return the node
	 */
	private Node buildTopBar()
	{
		VBox outer = new VBox(10);
		outer.setPadding(new Insets(0, 0, 6, 0));
		
		Label heading = new Label(this.headingText);
		heading.getStyleClass().add("journal-entry-heading");
		
		HBox titleRow = new HBox(12, heading);
		titleRow.setAlignment(Pos.CENTER_LEFT);
		
		Label dateLabel = boldLabel("Date");
		Label memoLabel = boldLabel("Memo");
		
		this.memoArea.setPrefRowCount(2);
		this.memoArea.setWrapText(true);
		this.memoArea.setMaxWidth(Double.MAX_VALUE);
		this.datePicker.setMaxWidth(Double.MAX_VALUE);
		
		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(8);
		
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setHgrow(Priority.NEVER);
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setHgrow(Priority.SOMETIMES);
		c2.setPercentWidth(20);
		ColumnConstraints c3 = new ColumnConstraints();
		c3.setHgrow(Priority.NEVER);
		ColumnConstraints c4 = new ColumnConstraints();
		c4.setHgrow(Priority.ALWAYS);
		c4.setPercentWidth(80);
		
		form.getColumnConstraints().setAll(c1, c2, c3, c4);
		
		form.add(dateLabel, 0, 0);
		form.add(this.datePicker, 1, 0);
		form.add(memoLabel, 2, 0);
		form.add(this.memoArea, 3, 0);
		GridPane.setHgrow(this.memoArea, Priority.ALWAYS);
		
		HBox totalsRow = new HBox(16,
			labelledValue("Debit", this.debitTotalLabel),
			labelledValue("Credit", this.creditTotalLabel),
			labelledValue("Difference", this.differenceLabel),
			this.statusBadge
		);
		totalsRow.setAlignment(Pos.CENTER_LEFT);
		
		this.statusBadge.getStyleClass().add("status-badge");
		this.statusBadge.setPadding(new Insets(4, 12, 4, 12));
		this.statusBadge.getStyleClass().add("state-neutral");
		
		this.validationMessage.setWrapText(true);
		this.validationMessage.getStyleClass().addAll("state-inline",
			"state-warning");
		
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		this.saveButton.setDefaultButton(true);
		this.saveButton.setOnAction(e -> persist());
		
		HBox actionRow = new HBox(12, totalsRow, spacer, this.saveButton);
		actionRow.setAlignment(Pos.CENTER_LEFT);
		
		outer.getChildren().addAll(
			titleRow,
			form,
			actionRow,
			this.validationMessage,
			new Separator()
		);
		
		return outer;
	}
	
	
	/**
	 * Bold label.
	 *
	 * @param text the text
	 * @return the label
	 */
	private static Label boldLabel(String text)
	{
		Label label = new Label(text);
		label.getStyleClass().add("field-label");
		return label;
	}
	
	
	/**
	 * Builds the lines workspace.
	 *
	 * @return the node
	 */
	private Node buildLineControls()
	{
		VBox block = new VBox(10);
		block.setAlignment(Pos.TOP_LEFT);

		Label heading = sectionHeading("Entry Lines");

		ToolBar toolbar =
			new ToolBar(this.addLineButton, this.duplicateLineButton,
				this.removeLineButton);
		toolbar.getStyleClass().add("toolbar-plain");

		this.addLineButton.setGraphic(null);
		this.addLineButton.getStyleClass().add("btn-add-line");
		this.addLineButton.setOnAction(e -> addLine());

		this.duplicateLineButton.setOnAction(e -> duplicateSelectedLine());
		this.removeLineButton.setOnAction(e -> removeSelectedLines());

		block.getChildren().addAll(heading, toolbar);
		return block;
	}

	/**
	 * Builds the party document card.
	 *
	 * @return the node
	 */
	private Node buildPartyDocumentCard()
	{
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		
		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHgrow(Priority.NEVER);
		ColumnConstraints fieldCol = new ColumnConstraints();
		fieldCol.setHgrow(Priority.ALWAYS);
		fieldCol.setFillWidth(true);
		
		grid.getColumnConstraints().setAll(labelCol, fieldCol);
		
		addDetailField(grid, 0, "To / From", this.toFromField);
		addDetailField(grid, 1, "Check #", this.checkNumberField);
		
		return card("Party / Document", grid);
	}
	
	
	/**
	 * Builds the bank card.
	 *
	 * @return the node
	 */
	private Node buildBankCard()
	{
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		
		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHgrow(Priority.NEVER);
		ColumnConstraints fieldCol = new ColumnConstraints();
		fieldCol.setHgrow(Priority.ALWAYS);
		fieldCol.setFillWidth(true);
		
		grid.getColumnConstraints().setAll(labelCol, fieldCol);
		
		addDetailField(grid, 0, "Bank", this.bankField);
		addDetailField(grid, 1, "Clearing Bank", this.clearBankField);
		addDetailField(grid, 2, "Reconciliation", this.reconciledCheckBox);
		
		return card("Bank / Reconciliation", grid);
	}
	
	

	/**
	 * Builds the donation subschedule panel hosted by the journal editor.
	 *
	 * @return the node
	 */
	private Node buildDonationSchedulePanel()
	{
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		grid.getColumnConstraints().setAll(new ColumnConstraints(),
			new ColumnConstraints());

		Button editDonor = new Button("Edit Selected Donor");
		editDonor.setOnAction(e -> editSelectedDonor());
		addDetailField(grid, 0, "Use Donation Schedule", this.donationScheduleEnabled);
		addDetailField(grid, 1, "Donation ID", this.donationIdField);
		addDetailField(grid, 2, "Donor ID", this.donorIdBox);
		addDetailField(grid, 3, "Donor Name", this.donorNameBox);
		addDetailField(grid, 4, "Donor", editDonor);
		return card("Donation Subschedule", grid);
	}

	/**
	 * Builds the supplemental panel.
	 *
	 * @return the node
	 */
	private Node buildSupplementalPanel()
	{
		VBox block = new VBox(8);
		block.setAlignment(Pos.TOP_LEFT);
		
		Label heading = sectionHeading("Supplemental Schedules");
		FlowPane toggles = new FlowPane(8, 8);
		toggles.setAlignment(Pos.CENTER_LEFT);
		
		for (SupplementalLineKind kind : SupplementalLineKind.values())
		{
			CheckBox checkBox = new CheckBox(formatSupplementalKindLabel(kind));
			checkBox.setOnAction(e -> updateSupplementalTabAvailability());
			this.supplementalSelections.put(kind, checkBox);
			toggles.getChildren().add(checkBox);
		}
		
		this.supplementalTabs.setMinHeight(180);
		VBox.setVgrow(this.supplementalTabs, Priority.ALWAYS);
		
		block.getChildren().addAll(buildDonationSchedulePanel(), heading, toggles,
			this.supplementalTabs);
		return block;
		
	}
	
	/**
	 * Format supplemental line kind label.
	 *
	 * @param kind the kind
	 * @return the label
	 */
	private static String formatSupplementalKindLabel(SupplementalLineKind kind)
	{
		String base = kind.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
		String[] words = base.split("\\s+");
		StringBuilder out = new StringBuilder();
		
		for (String word : words)
		{
			if (word == null || word.isBlank())
			{
				continue;
			}
			
			if (out.length() > 0)
			{
				out.append(' ');
			}
			
			out.append(Character.toUpperCase(word.charAt(0)));
			
			if (word.length() > 1)
			{
				out.append(word.substring(1));
			}
		}
		
		return out.toString();
	}
	/**
	 * Builds the budget card.
	 *
	 * @return the node
	 */
	private Node buildBudgetCard()
	{
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		
		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setHgrow(Priority.NEVER);
		ColumnConstraints fieldCol = new ColumnConstraints();
		fieldCol.setHgrow(Priority.ALWAYS);
		fieldCol.setFillWidth(true);
		
		grid.getColumnConstraints().setAll(labelCol, fieldCol);
		
		addDetailField(grid, 0, "Budget Tracking", this.budgetTrackingField);
		addDetailField(grid, 1, "Fund Name", this.associatedFundNameField);
		
		return card("Budget / Fund", grid);
	}
	
	/**
	 * Builds the metadata area.
	 *
	 * @return the node
	 */
	private Node buildMetadataArea()
	{
		VBox wrapper = new VBox(12);
		
		Label heading = sectionHeading("Additional Details");
		
		HBox row = new HBox(12,
			buildPartyDocumentCard(),
			buildBankCard(),
			buildBudgetCard()
		);
		row.setAlignment(Pos.TOP_LEFT);
		
		for (Node child : row.getChildren())
		{
			if (child instanceof Region region)
			{
				region.setMaxWidth(Double.MAX_VALUE);
				HBox.setHgrow(region, Priority.ALWAYS);
			}
		}
		
		wrapper.getChildren().addAll(heading, row);
		return wrapper;
	}
	
	

	private void configureDonationSchedule()
	{
		this.donationIdField.setPromptText("Auto-generated when enabled");
		this.donorIdBox.setItems(this.donorChoices);
		this.donorNameBox.setItems(this.donorChoices);
		this.donorIdBox.setPromptText("Select donor id");
		this.donorNameBox.setPromptText("Select donor name");
		this.donorIdBox.setConverter(donorConverter(true));
		this.donorNameBox.setConverter(donorConverter(false));
		this.donorIdBox.setOnShowing(e -> refreshDonorChoices());
		this.donorNameBox.setOnShowing(e -> refreshDonorChoices());
		this.donorIdBox.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && this.donorNameBox.getValue() != newValue)
			{
				this.donorNameBox.setValue(newValue);
			}
		});
		this.donorNameBox.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null && this.donorIdBox.getValue() != newValue)
			{
				this.donorIdBox.setValue(newValue);
			}
		});
		this.donationScheduleEnabled.selectedProperty().addListener(
			(obs, oldValue, enabled) -> updateDonationScheduleEnabled());
		refreshDonorChoices();
		updateDonationScheduleEnabled();
	}

	private StringConverter<DonorContact> donorConverter(boolean idFirst)
	{
		return new StringConverter<>()
		{
			@Override
			public String toString(DonorContact donor)
			{
				if (donor == null)
				{
					return "";
				}
				String id = donor.getId() == null ? "" : donor.getId();
				String name = donor.getName() == null ? "" : donor.getName();
				return idFirst ? id + " — " + name : name + " — " + id;
			}

			@Override
			public DonorContact fromString(String value)
			{
				return null;
			}
		};
	}

	private void refreshDonorChoices()
	{
		DonorContact selected = selectedDonor();
		try
		{
			DonorService service = new DonorService();
			service.loadDonors(null);
			this.donorChoices.setAll(service.getAllDonors().stream()
				.filter(donor -> donor.getId() != null && !donor.getId().isBlank())
				.sorted((a, b) -> donorSortKey(a).compareToIgnoreCase(donorSortKey(b)))
				.toList());
			selectDonor(selected == null ? null : selected.getId());
		}
		catch (IOException ex)
		{
			this.donorChoices.clear();
			this.donorIdBox.setPromptText("Unable to load donors");
			this.donorNameBox.setPromptText("Unable to load donors");
		}
	}

	private static String donorSortKey(DonorContact donor)
	{
		String name = donor.getName() == null ? "" : donor.getName();
		String id = donor.getId() == null ? "" : donor.getId();
		return name + " " + id;
	}

	private void selectDonor(String donorId)
	{
		if (donorId != null)
		{
			for (DonorContact donor : this.donorChoices)
			{
				if (donorId.equals(donor.getId()))
				{
					this.donorIdBox.setValue(donor);
					this.donorNameBox.setValue(donor);
					return;
				}
			}
		}
		this.donorIdBox.setValue(null);
		this.donorNameBox.setValue(null);
	}

	private DonorContact selectedDonor()
	{
		DonorContact donor = this.donorIdBox.getValue();
		return donor == null ? this.donorNameBox.getValue() : donor;
	}

	private void updateDonationScheduleEnabled()
	{
		boolean enabled = this.donationScheduleEnabled.isSelected();
		this.donationIdField.setDisable(!enabled);
		this.donorIdBox.setDisable(!enabled);
		this.donorNameBox.setDisable(!enabled);
		if (enabled && this.donationIdField.getText().isBlank())
		{
			this.donationIdField.setText(UUID.randomUUID().toString());
		}
	}

	private void editSelectedDonor()
	{
		DonorContact selected = selectedDonor();
		if (selected == null)
		{
			AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
				"Select a donor first.");
			return;
		}

		Dialog<DonorContact> dialog = new Dialog<>();
		dialog.setTitle("Edit Donor");
		dialog.setHeaderText("Edit donor " + selected.getId());
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK,
			ButtonType.CANCEL);
		TextField idField = new TextField(selected.getId());
		idField.setEditable(false);
		TextField nameField = new TextField(selected.getName());
		TextField emailField = new TextField(selected.getEmail());
		TextField phoneField = new TextField(selected.getPhone());
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		grid.setPadding(PanelChrome.PANEL_PADDING);
		grid.addRow(0, new Label("Donor ID:"), idField);
		grid.addRow(1, new Label("Name:"), nameField);
		grid.addRow(2, new Label("Email:"), emailField);
		grid.addRow(3, new Label("Phone:"), phoneField);
		dialog.getDialogPane().setContent(grid);
		dialog.setResultConverter(button -> {
			if (button != ButtonType.OK)
			{
				return null;
			}
			return new DonorContact(selected.getId(), nameField.getText(),
				emailField.getText(), phoneField.getText());
		});
		dialog.showAndWait().ifPresent(updated -> {
			DonorService service = new DonorService();
			service.editDonor(selected.getId(), updated);
			refreshDonorChoices();
			selectDonor(updated.getId());
		});
	}

	private void configureFundSelector()
	{
		this.associatedFundNameField.setItems(this.fundNameChoices);
		this.associatedFundNameField.setPromptText("Select fund");
		this.associatedFundNameField.setOnShowing(e -> refreshFundChoices());
		refreshFundChoices();
	}

	private void refreshFundChoices()
	{
		try
		{
			String selected = this.associatedFundNameField.getValue();
			this.fundNameChoices.setAll(FundNameLookup.listActiveFundNames());
			selectFundName(selected);
		}
		catch (SQLException ex)
		{
			this.fundNameChoices.clear();
			this.associatedFundNameField.setPromptText("Unable to load funds");
		}
	}

	private void selectFundName(String fundName)
	{
		if (fundName != null && this.fundNameChoices.contains(fundName))
		{
			this.associatedFundNameField.getSelectionModel().select(fundName);
		}
		else
		{
			this.associatedFundNameField.getSelectionModel().clearSelection();
			this.associatedFundNameField.setValue(null);
		}
	}

	private String selectedFundName()
	{
		String value = this.associatedFundNameField.getValue();
		return value == null ? "" : value.trim();
	}

	/**
	 * Card.
	 *
	 * @param title the title
	 * @param content the content
	 * @return the node
	 */
	private Node card(String title, Node content)
	{
		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("card-title");
		
		VBox box = new VBox(10, titleLabel, content);
		box.setPadding(new Insets(12));
		box.getStyleClass().add("npbk-card");
		
		return box;
	}
	
	/**
	 * Section heading.
	 *
	 * @param text the text
	 * @return the label
	 */
	private Label sectionHeading(String text)
	{
		Label label = new Label(text);
		label.getStyleClass().add("section-heading");
		return label;
		
	}
	
	/**
	 * Builds the table section.
	 *
	 * @return the node
	 */
	private Node buildTableSection()
	{
		VBox block = new VBox(10);
		block.setAlignment(Pos.TOP_LEFT);
		
		ToolBar toolbar =
			new ToolBar(this.addLineButton, this.duplicateLineButton,
				this.removeLineButton);
		toolbar.getStyleClass().add("toolbar-plain");
		
		this.addLineButton.setGraphic(null);
		this.addLineButton.getStyleClass().add("btn-add-line");
		this.addLineButton.setOnAction(e -> addLine());
		
		this.duplicateLineButton.setOnAction(e -> duplicateSelectedLine());
		this.removeLineButton.setOnAction(e -> removeSelectedLines());
		
		configureTable();
		this.table.setMinHeight(180);
		VBox.setVgrow(this.table, Priority.ALWAYS);
		
		block.getChildren().addAll(toolbar, this.table);
		return block;
	}
	
	/**
	 * Builds the supplemental section.
	 *
	 * @return the node
	 */
	private Node buildSupplementalSection()
	{
		VBox block = new VBox(8);
		block.setAlignment(Pos.TOP_LEFT);
		this.supplementalTabs.setMinHeight(140);
		block.getChildren().add(this.supplementalTabs);
		VBox.setVgrow(this.supplementalTabs, Priority.ALWAYS);
		return block;
	}
	
	/**
	 * Builds the details section.
	 *
	 * @return the node
	 */
	private Node buildDetailsSection()
	{
		GridPane grid = new GridPane();
		grid.setHgap(12);
		grid.setVgap(8);
		
		ColumnConstraints labelColumn = new ColumnConstraints();
		labelColumn.setMinWidth(Region.USE_PREF_SIZE);
		labelColumn.setPrefWidth(Region.USE_PREF_SIZE);
		labelColumn.setMaxWidth(Region.USE_PREF_SIZE);
		labelColumn.setHgrow(Priority.NEVER);
		
		ColumnConstraints fieldColumn = new ColumnConstraints();
		fieldColumn.setHgrow(Priority.ALWAYS);
		
		grid.getColumnConstraints().addAll(labelColumn, fieldColumn);
		
		int row = 0;
		addDetailField(grid, row++, "Date", this.datePicker);
		addDetailField(grid, row++, "To / From", this.toFromField);
		addDetailField(grid, row++, "Memo", this.memoArea);
		addDetailField(grid, row++, "Check #", this.checkNumberField);
		addDetailField(grid, row++, "Clearing Bank", this.clearBankField);
		addDetailField(grid, row++, "Bank", this.bankField);
		addDetailField(grid, row++, "Reconciliation", this.reconciledCheckBox);
		addDetailField(grid, row++, "Budget Tracking",
			this.budgetTrackingField);
		addDetailField(grid, row++, "Fund Name", this.associatedFundNameField);
		
		return grid;
		
	}
	

	
	/**
	 * Labelled value.
	 *
	 * @param labelText the label text
	 * @param valueLabel the value label
	 * @return the h box
	 */
	private static HBox labelledValue(String labelText, Label valueLabel)
	{
		Label label = new Label(labelText + ":");
		label.getStyleClass().add("value-label");
		HBox box = new HBox(6, label, valueLabel);
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
		
	}
	
	
	/**
	 * Adds the detail field.
	 *
	 * @param grid the grid
	 * @param row the row
	 * @param labelText the label text
	 * @param field the field
	 */
	private static void addDetailField(GridPane grid, int row, String labelText, Node field)
	{
		Label label = new Label(labelText);
		label.getStyleClass().add("field-label");
		grid.add(label, 0, row);
		grid.add(field, 1, row);
		GridPane.setHgrow(field, Priority.ALWAYS);
		
		if (field instanceof TextArea ta)
		{
			ta.setPrefRowCount(2);
		}
		
		if (field instanceof ComboBox<?> combo)
		{
			combo.setMaxWidth(Double.MAX_VALUE);
		}
		
		if (field instanceof TextField tf)
		{
			tf.setPromptText(labelText);
		}
		
		if (field instanceof Region region)
		{
			region.setMaxWidth(Double.MAX_VALUE);
		}
	}
	/**
	 * Configure table.
	 */
	private void configureTable()
	{
		this.table.setEditable(true);
		this.table.setColumnResizePolicy(
			TableView.UNCONSTRAINED_RESIZE_POLICY);
		
		TableColumn<Line, String> accountCol = new TableColumn<>("Account");
		accountCol.setPrefWidth(640);
		accountCol.setCellValueFactory(cd -> cd.getValue().account);
		accountCol.setCellFactory(accountCellFactory());
		accountCol.setOnEditCommit(ev -> {
			Line row = ev.getRowValue();
			row.account.set(ev.getNewValue());
			adjustForAccountSide(row);
			refreshAfterEdit();
		});
		
		TableColumn<Line, BigDecimal> debitCol = amtCol("Debit", l -> l.debit);
		TableColumn<Line, BigDecimal> creditCol =
			amtCol("Credit", l -> l.credit);
		debitCol.setPrefWidth(240);
		creditCol.setPrefWidth(240);
		
		debitCol.setOnEditCommit(ev -> {
			Line row = ev.getRowValue();
			row.debit.set(amountOrZero(ev.getNewValue()));
			
			if (amountOrZero(row.debit.get()).signum() != 0)
			{
				row.credit.set(BigDecimal.ZERO);
			}
			
			adjustForAccountSide(row);
			refreshAfterEdit();
		});
		
		creditCol.setOnEditCommit(ev -> {
			Line row = ev.getRowValue();
			row.credit.set(amountOrZero(ev.getNewValue()));
			
			if (amountOrZero(row.credit.get()).signum() != 0)
			{
				row.debit.set(BigDecimal.ZERO);
			}
			
			adjustForAccountSide(row);
			refreshAfterEdit();
		});
		
		this.table.getColumns().setAll(accountCol, debitCol, creditCol);
		this.table.setRowFactory(tv -> {
			TableRow<Line> row = new TableRow<>();
			row.setOnMouseClicked(e -> {
				
				if (e.getClickCount() == 1 && !row.isEmpty())
				{
					this.table.edit(row.getIndex(), accountCol);
				}
				
			});
			return row;
		});
		
	}
	
	/**
	 * Account cell factory.
	 *
	 * @return the callback
	 */
	private
		Callback<TableColumn<Line, String>, TableCell<Line, String>>
		accountCellFactory()
	{
		ObservableList<String> choices = FXCollections.observableArrayList(
			this.accountsByName.keySet());
		
		StringConverter<String> converter = new StringConverter<>()
		{
			@Override
			public String toString(String object)
			{
				return object;
				
			}
			
			@Override
			public String fromString(String string)
			{
				return string;
				
			}
			
		};
		
		return column -> new ComboBoxTableCell<>(converter, choices)
		{
			@Override
			public void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				
				if (empty)
				{
					getStyleClass().remove(ACCOUNT_CELL_MISSING_CLASS);
					setTooltip(null);
					return;
				}
				
				Line line =
					getTableRow() != null ? getTableRow().getItem() : null;
				Account account = line != null ?
					resolveAccount(line.account.get()) : resolveAccount(item);
				
				boolean highlight = line != null && account == null &&
					(amountOrZero(line.debit.get()).signum() != 0 ||
						amountOrZero(line.credit.get()).signum() != 0);
				
				if (highlight)
				{
					if (!getStyleClass().contains(ACCOUNT_CELL_MISSING_CLASS))
					{
						getStyleClass().add(ACCOUNT_CELL_MISSING_CLASS);
					}
					setTooltip(new Tooltip(
						"Account not found in the chart of accounts"));
				}
				else
				{
					getStyleClass().remove(ACCOUNT_CELL_MISSING_CLASS);
					setTooltip(null);
				}
				
			}
			
		};
		
	}
	
	/**
	 * Amt col.
	 *
	 * @param title the title
	 * @param prop the prop
	 * @return the table column
	 */
	private TableColumn<Line, BigDecimal> amtCol(String title,
		Callback<Line, Property<BigDecimal>> prop)
	{
		TableColumn<Line, BigDecimal> c = new TableColumn<>(title);
		c.setCellValueFactory(cell -> prop.call(cell.getValue()));
		c.setCellFactory(param -> new FocusCommitTextFieldTableCell<>(
			createCurrencyConverter()));
		c.setEditable(true);
		c.getStyleClass().add("table-column-numeric");
		return c;
		
	}
	
	/**
	 * Creates the currency converter.
	 *
	 * @return the string converter
	 */
	private static StringConverter<BigDecimal> createCurrencyConverter()
	{
		return new StringConverter<>()
		{
			@Override
			public String toString(BigDecimal value)
			{
				
				if (value == null)
				{
					return "";
				}
				
				return FormatUtils.formatCurrency(value);
				
			}
			
			@Override
			public BigDecimal fromString(String value)
			{
				
				if (value == null || value.isBlank())
				{
					return null;
				}
				
				BigDecimal parsed = FormatUtils.parseCurrency(value);
				
				if (parsed == null)
				{
					throw new IllegalArgumentException(
						"Invalid currency amount: " + value);
				}
				
				return parsed;
				
			}
			
		};
		
	}
	
	/**
	 * Attach listeners.
	 */
	private void attachListeners()
	{
		this.lines.addListener((ListChangeListener<Line>) change -> {
			
			while (change.next())
			{
				
				if (change.wasAdded())
				{
					change.getAddedSubList().forEach(this::watchLine);
				}
				
			}
			
			refreshAfterEdit();
		});
		
		this.memoArea.textProperty()
			.addListener((obs, o, n) -> markValidationPending());
		
	}
	
	/**
	 * Watch line.
	 *
	 * @param line the line
	 */
	private void watchLine(Line line)
	{
		line.account.addListener((obs, o, n) -> {
			adjustForAccountSide(line);
			refreshAfterEdit();
		});
		line.debit.addListener((obs, o, n) -> refreshAfterEdit());
		line.credit.addListener((obs, o, n) -> refreshAfterEdit());
		
	}
	
	/**
	 * Refresh after edit.
	 */
	private void refreshAfterEdit()
	{
		recalcTotals();
		markValidationPending();
		this.table.refresh();
		updateSupplementalTabAvailability();
		
	}
	
	/**
	 * Load person refs.
	 *
	 * @return the list
	 */
	private List<PersonRef> loadPersonRefs()
	{
		
		if (!Database.isInitialized())
		{
			return List.of();
		}
		
		PersonService personService = new PersonService();
		List<PersonRef> refs = new ArrayList<>();
		
		for (Person person : personService.listPeople())
		{
			refs.add(new PersonRef(person.getId(), person.getName()));
		}
		
		return refs;
		
	}
	
	/**
	 * Load entry refs.
	 *
	 * @param txnId the txn id
	 * @return the list
	 */
	private List<EntryRef> loadEntryRefs(long txnId)
	{
		
		if (!Database.isInitialized() || txnId <= 0)
		{
			return List.of();
		}
		
		String sql =
			"SELECT id, amount, account_name, account_number, account_side " +
				"FROM journal_entry WHERE txn_id = ? ORDER BY id";
		
		List<EntryRef> refs = new ArrayList<>();
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(sql))
		{
			ps.setLong(1, txnId);
			
			try (ResultSet rs = ps.executeQuery())
			{
				
				while (rs.next())
				{
					String accountName = rs.getString("account_name");
					
					if (accountName == null || accountName.isBlank())
					{
						accountName = rs.getString("account_number");
					}
					
					AccountSide side =
						AccountSide.fromString(rs.getString("account_side"));
					boolean debit = side == AccountSide.DEBIT;
					refs.add(new EntryRef(
						rs.getLong("id"),
						accountName,
						debit,
						rs.getBigDecimal("amount")));
				}
				
			}
			
		}
		catch (SQLException ex)
		{
			return Collections.emptyList();
		}
		
		return refs;
		
	}
	
	/**
	 * Load entry amounts by id.
	 *
	 * @return the map
	 */
	private Map<Long, BigDecimal> loadEntryAmountsById()
	{
		
		if (!Database.isInitialized())
		{
			return Map.of();
		}
		
		Set<Long> entryIds = new LinkedHashSet<>();
		
		for (SupplementalLineKind kind : SupplementalLineKind.values())
		{
			SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
			
			if (editor == null)
			{
				continue;
			}
			
			for (SupplementalLineRow row : editor.getRows())
			{
				Long entryId = row.getEntryId();
				
				if (entryId != null)
				{
					entryIds.add(entryId);
				}
				
			}
			
		}
		
		if (entryIds.isEmpty())
		{
			return Map.of();
		}
		
		String placeholders = entryIds.stream()
			.map(id -> "?")
			.collect(Collectors.joining(", "));
		String sql = "SELECT id, amount FROM journal_entry WHERE id IN (" +
			placeholders + ")";
		
		Map<Long, BigDecimal> amounts = new HashMap<>();
		
		try (Connection c = Database.get().getConnection();
			PreparedStatement ps = c.prepareStatement(sql))
		{
			int index = 1;
			
			for (Long entryId : entryIds)
			{
				ps.setLong(index++, entryId);
			}
			
			try (ResultSet rs = ps.executeQuery())
			{
				
				while (rs.next())
				{
					amounts.put(rs.getLong("id"), rs.getBigDecimal("amount"));
				}
				
			}
			
		}
		catch (SQLException ex)
		{
			return Map.of();
		}
		
		return amounts;
		
	}
	
	/**
	 * Update supplemental tab availability.
	 */
	private void updateSupplementalTabAvailability()
	{
		for (SupplementalLineKind kind : SupplementalLineKind.values())
		{
			this.supplementalTabs.setEnabled(kind,
				isSupplementalSelected(kind));
		}
		
	}
	
	/**
	 * Returns whether the supplemental checkbox is selected.
	 *
	 * @param kind the kind
	 * @return true, if selected
	 */
	private boolean isSupplementalSelected(SupplementalLineKind kind)
	{
		CheckBox checkBox = this.supplementalSelections.get(kind);
		return checkBox != null && checkBox.isSelected();
	}
	
	/**
	 * Apply default accounts from settings.
	 */
	private void applyDefaultAccountsFromSettings()
	{
		
		if (!Database.isInitialized())
		{
			return;
		}
		
		SettingsService settingsService = new SettingsService();
		
		try
		{
			settingsService.loadSettings(null);
		}
		catch (IOException ex)
		{
			return;
		}
		
		SettingsModel settings = settingsService.getSettings();
		
		if (settings == null || this.lines.isEmpty())
		{
			return;
		}
		
		if (settings.getDefaultExpenseAccount() != null &&
			!settings.getDefaultExpenseAccount().isBlank())
		{
			this.lines.get(0).account.set(settings.getDefaultExpenseAccount());
		}
		
		if (settings.getDefaultIncomeAccount() != null &&
			!settings.getDefaultIncomeAccount().isBlank())
		{
			
			if (this.lines.size() == 1)
			{
				addLine();
			}
			
			if (this.lines.size() >= 2)
			{
				this.lines.get(1).account
					.set(settings.getDefaultIncomeAccount());
			}
			
		}
		
		if (!this.lines.isEmpty())
		{
			this.table.getSelectionModel().select(this.lines.get(0));
		}
		
	}
	
	/**
	 * Adds the line.
	 */
	private void addLine()
	{
		Line line = new Line();
		this.lines.add(line);
		this.table.getSelectionModel().select(line);
		this.table.scrollTo(line);
		
	}
	
	/**
	 * Duplicate selected line.
	 */
	private void duplicateSelectedLine()
	{
		Line selected = this.table.getSelectionModel().getSelectedItem();
		
		if (selected == null)
		{
			return;
		}
		
		Line copy = new Line();
		copy.account.set(selected.account.get());
		copy.debit.set(amountOrZero(selected.debit.get()));
		copy.credit.set(amountOrZero(selected.credit.get()));
		this.lines.add(this.lines.indexOf(selected) + 1, copy);
		this.table.getSelectionModel().select(copy);
		
	}
	
	/**
	 * Removes the selected lines.
	 */
	private void removeSelectedLines()
	{
		List<Line> selected =
			new ArrayList<>(this.table.getSelectionModel().getSelectedItems());
		
		if (selected.isEmpty())
		{
			return;
		}
		
		this.lines.removeAll(selected);
		
		if (this.lines.isEmpty())
		{
			addLine();
		}
		
	}
	
	/**
	 * Recalc totals.
	 */
	private void recalcTotals()
	{
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		
		for (Line l : this.lines)
		{
			debit = debit.add(amountOrZero(l.debit.get()));
			credit = credit.add(amountOrZero(l.credit.get()));
		}
		
		BigDecimal diff = debit.subtract(credit);
		
		this.debitTotalLabel.setText(FormatUtils.formatCurrency(debit));
		this.creditTotalLabel.setText(FormatUtils.formatCurrency(credit));
		this.differenceLabel.setText(FormatUtils.formatCurrency(diff.abs()));
		
	}
	
	/**
	 * Mark validation pending.
	 */
	private void markValidationPending()
	{
		this.saveButton.setDisable(false);
		this.validationMessage
			.setText("Press Save to validate the entry totals.");
		this.saveButton.setTooltip(null);
		setStatusBadgeState("Pending check", "state-neutral");
		setValidationStateClass("state-warning");
		
	}
	
	/**
	 * Show validation error.
	 *
	 * @param message the message
	 */
	private void showValidationError(String message)
	{
		this.saveButton.setDisable(false);
		this.validationMessage.setText(message);
		this.saveErrorTooltip.setText(message);
		this.saveButton.setTooltip(this.saveErrorTooltip);
		setStatusBadgeState("Needs attention", "state-error");
		setValidationStateClass("state-error");
		
	}
	
	/**
	 * Show balanced state.
	 */
	private void showBalancedState()
	{
		this.saveButton.setDisable(false);
		this.validationMessage.setText("");
		this.saveButton.setTooltip(null);
		setStatusBadgeState("Balanced", "state-valid");
		setValidationStateClass("state-valid");
		
	}

	/**
	 * Applies a status class and text to the status badge.
	 *
	 * @param text badge text
	 * @param stateClass status class
	 */
	private void setStatusBadgeState(String text, String stateClass)
	{
		this.statusBadge.setText(text);
		this.statusBadge.getStyleClass().removeAll(
			"state-neutral", "state-error", "state-valid", "state-warning");
		this.statusBadge.getStyleClass().add(stateClass);
	}

	/**
	 * Applies shared inline state classes to the validation message.
	 *
	 * @param stateClass semantic state class
	 */
	private void setValidationStateClass(String stateClass)
	{
		this.validationMessage.getStyleClass().removeAll("state-neutral",
			"state-error", "state-valid", "state-warning");
		this.validationMessage.getStyleClass().add(stateClass);
	}
	
	/**
	 * Validate lines.
	 *
	 * @return the optional
	 */
	private Optional<String> validateLines()
	{
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		List<String> missingAccounts = new ArrayList<>();
		boolean hasAmounts = false;
		
		for (Line l : this.lines)
		{
			BigDecimal debitAmount = amountOrZero(l.debit.get());
			BigDecimal creditAmount = amountOrZero(l.credit.get());
			boolean hasValue =
				debitAmount.signum() != 0 || creditAmount.signum() != 0;
			String accountToken = l.account.get();
			
			if (!hasValue && (accountToken == null || accountToken.isBlank()))
			{
				continue;
			}
			
			if (accountToken == null || accountToken.isBlank())
			{
				return Optional.of("Each amount must reference an account.");
			}
			
			if (debitAmount.signum() != 0 && creditAmount.signum() != 0)
			{
				return Optional.of(
					"A line cannot contain both a debit and a credit amount.");
			}
			
			Account account = resolveAccount(accountToken);
			
			if (account == null)
			{
				missingAccounts.add(accountToken);
				continue;
			}
			
			hasAmounts |= hasValue;
			
			if (debitAmount.signum() != 0)
			{
				debit = debit.add(debitAmount);
			}
			
			if (creditAmount.signum() != 0)
			{
				credit = credit.add(creditAmount);
			}
			
		}
		
		if (!missingAccounts.isEmpty())
		{
			return Optional
				.of("Account not found: " + String.join(", ", missingAccounts));
		}
		
		if (!hasAmounts)
		{
			return Optional.of("Add at least one debit or credit amount.");
		}
		
		if (debit.compareTo(credit) != 0)
		{
			return Optional.of("Transaction is not balanced.");
		}
		
		return Optional.empty();
		
	}
	
	/**
	 * Validate supplemental lines.
	 *
	 * @return the optional
	 */
	private Optional<String> validateSupplementalLines()
	{
		List<String> errors = new ArrayList<>();
		Map<Long, BigDecimal> entryAmounts = loadEntryAmountsById();
		
		for (SupplementalLineKind kind : SupplementalLineKind.values())
		{
			SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
			
			if (editor == null)
			{
				continue;
			}
			
			if (!editor.validateAndDisplay())
			{
				errors.add("Fix validation errors in the " +
					editor.getConfig().tabTitle + " tab.");
			}
			
			if (!entryAmounts.isEmpty())
			{
				errors.addAll(editor.validateEntryLinkSums(entryAmounts::get));
			}
			
			BigDecimal expected = expectedAmountForKind(kind);
			BigDecimal actual = sumSupplementalAmount(kind);
			
			if (expected.signum() != 0 || actual.signum() != 0)
			{
				
				if (expected.compareTo(actual) != 0)
				{
					errors.add(editor.getConfig().tabTitle + " total " +
						actual + " must match entry total " + expected + ".");
				}
				
			}
			
		}
		
		if (errors.isEmpty())
		{
			return Optional.empty();
		}
		
		return Optional.of(String.join("\n", errors));
		
	}
	
	/**
	 * Expected amount for kind.
	 *
	 * @param kind the kind
	 * @return the big decimal
	 */
	private BigDecimal expectedAmountForKind(SupplementalLineKind kind)
	{
		if (!isSupplementalSelected(kind))
		{
			return BigDecimal.ZERO;
		}
		
		BigDecimal total = BigDecimal.ZERO;
		
		for (Line line : this.lines)
		{
			Account account = resolveAccount(line.account.get());
			
			if (account == null)
			{
				continue;
			}
			
			BigDecimal debitAmount = amountOrZero(line.debit.get());
			BigDecimal creditAmount = amountOrZero(line.credit.get());
			
			if (isAssetKind(kind))
			{
				total = total.add(debitAmount);
			}
			else
			{
				total = total.add(creditAmount);
			}
			
		}
		
		return total;
		
	}
	
	/**
	 * Sum supplemental amount.
	 *
	 * @param kind the kind
	 * @return the big decimal
	 */
	private BigDecimal sumSupplementalAmount(SupplementalLineKind kind)
	{
		SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
		
		if (editor == null)
		{
			return BigDecimal.ZERO;
		}
		
		BigDecimal total = BigDecimal.ZERO;
		
		for (SupplementalLineRow row : editor.getRows())
		{
			BigDecimal amount = row.getAmount();
			
			if (amount != null)
			{
				total = total.add(amount);
			}
			
		}
		
		return total;
		
	}
	
	/**
	 * Checks if is asset kind.
	 *
	 * @param kind the kind
	 * @return true, if is asset kind
	 */
	private boolean isAssetKind(SupplementalLineKind kind)
	{
		return kind == SupplementalLineKind.RECEIVABLE ||
			kind == SupplementalLineKind.PREPAID_EXPENSE ||
			kind == SupplementalLineKind.OTHER_ASSET;
		
	}
	
	/**
	 * Collect supplemental lines.
	 *
	 * @return the list
	 */
	private List<TxnSupplementalLineBase> collectSupplementalLines()
	{
		List<TxnSupplementalLineBase> beans = new ArrayList<>();
		
		for (SupplementalLineKind kind : SupplementalLineKind.values())
		{
			SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
			
			if (editor == null)
			{
				continue;
			}
			
			beans.addAll(SupplementalLinesFxAdapter.toBeans(kind,
				new ArrayList<>(editor.getRows())));
		}
		
		return beans;
		
	}
	
	/**
	 * Load supplemental lines.
	 *
	 * @param tx the tx
	 */
	private void loadSupplementalLines(AccountingTransaction tx)
	{
		Map<SupplementalLineKind, List<SupplementalLineRow>> grouped =
			new EnumMap<>(SupplementalLineKind.class);
		
		for (SupplementalLineKind kind : SupplementalLineKind.values())
		{
			grouped.put(kind, new ArrayList<>());
		}
		
		if (tx.getSupplementalLines() != null)
		{
			
			for (TxnSupplementalLineBase line : tx.getSupplementalLines())
			{
				grouped.get(line.getKind()).add(
					SupplementalLinesFxAdapter.toRow(line));
			}
			
		}
		
		for (SupplementalLineKind kind : SupplementalLineKind.values())
		{
			SupplementalLinesEditor editor = this.supplementalTabs.editor(kind);
			
			if (editor != null)
			{
				editor.setRows(grouped.get(kind));
			}
			
			CheckBox selection = this.supplementalSelections.get(kind);
			
			if (selection != null)
			{
				selection.setSelected(!grouped.get(kind).isEmpty());
			}
			
		}
		
	}
	
	/**
	 * Adjust for account side.
	 *
	 * @param line the line
	 */
	private void adjustForAccountSide(Line line)
	{
		Account account = resolveAccount(line.account.get());
		
		if (account == null)
		{
			return;
		}
		
		BigDecimal debitAmount = amountOrZero(line.debit.get());
		BigDecimal creditAmount = amountOrZero(line.credit.get());
		
		if (account.getIncreaseSide() == AccountSide.DEBIT &&
			creditAmount.signum() != 0 && debitAmount.signum() == 0)
		{
			line.debit.set(creditAmount);
			line.credit.set(BigDecimal.ZERO);
		}
		else if (account.getIncreaseSide() == AccountSide.CREDIT &&
			debitAmount.signum() != 0 && creditAmount.signum() == 0)
		{
			line.credit.set(debitAmount);
			line.debit.set(BigDecimal.ZERO);
		}
		
	}
	

	private void applyDonationInfo(Map<String, String> info)
	{
		if (INFO_MODULE_DONATION.equalsIgnoreCase(info.get(INFO_MODULE)))
		{
			info.remove(INFO_MODULE);
		}
		info.remove(INFO_DONATION_ID);
		info.remove(INFO_DONOR_ID);
		info.remove(INFO_DONOR_NAME);
		if (!this.donationScheduleEnabled.isSelected())
		{
			return;
		}
		String donationId = this.donationIdField.getText() == null ? "" :
			this.donationIdField.getText().trim();
		if (donationId.isBlank())
		{
			donationId = UUID.randomUUID().toString();
			this.donationIdField.setText(donationId);
		}
		DonorContact donor = selectedDonor();
		if (donor == null)
		{
			throw new IllegalArgumentException(
				"Select a donor for the donation schedule.");
		}
		info.put(INFO_MODULE, INFO_MODULE_DONATION);
		info.put(INFO_DONATION_ID, donationId);
		info.put(INFO_DONOR_ID, donor.getId());
		info.put(INFO_DONOR_NAME, donor.getName() == null ? "" : donor.getName());
		if (this.toFromField.getText() == null || this.toFromField.getText().isBlank())
		{
			this.toFromField.setText(donor.getName());
		}
	}

	/**
	 * Persist.
	 */
	private void persist()
	{
		Optional<String> validationError = validateLines();
		
		if (validationError.isPresent())
		{
			showValidationError(validationError.get());
			AlertBox.showError(
				getScene() == null ? null : getScene().getWindow(),
				validationError.get());
			return;
		}
		
		Optional<String> supplementalError = validateSupplementalLines();
		
		if (supplementalError.isPresent())
		{
			AlertBox.showError(
				getScene() == null ? null : getScene().getWindow(),
				supplementalError.get());
			return;
		}
		
		if (this.donationScheduleEnabled.isSelected() && selectedDonor() == null)
		{
			String message = "Select a donor for the donation schedule.";
			showValidationError(message);
			AlertBox.showError(getScene() == null ? null : getScene().getWindow(),
				message);
			return;
		}

		showBalancedState();
		
		BigDecimal debit = BigDecimal.ZERO;
		BigDecimal credit = BigDecimal.ZERO;
		Set<AccountingEntry> entries = new LinkedHashSet<>();
		
		for (Line l : this.lines)
		{
			BigDecimal debitAmount = amountOrZero(l.debit.get());
			BigDecimal creditAmount = amountOrZero(l.credit.get());
			
			if (debitAmount.signum() == 0 && creditAmount.signum() == 0)
			{
				continue;
			}
			
			Account account = resolveAccount(l.account.get());
			
			if (account == null)
			{
				continue;
			}
			
			String acctNum = account.getAccountNumber();
			String acctName = account.getName();
			
			if (debitAmount.signum() != 0)
			{
				entries.add(new AccountingEntry(debitAmount, acctNum,
					AccountSide.DEBIT, acctName));
				debit = debit.add(debitAmount);
			}
			
			if (creditAmount.signum() != 0)
			{
				entries.add(new AccountingEntry(creditAmount, acctNum,
					AccountSide.CREDIT, acctName));
				credit = credit.add(creditAmount);
			}
			
		}
		
		Map<String, String> info = new HashMap<>();
		if (this.original != null && this.original.getInfo() != null)
		{
			info.putAll(this.original.getInfo());
		}
		applyDonationInfo(info);

		AccountingTransaction tx =
			new AccountingTransaction(new Account(), entries, info,
				this.original != null ?
					this.original.getBookingDateTimestamp() :
					Instant.now().toEpochMilli());
		
		if (this.original != null)
		{
			tx.setId(this.original.getId());
		}
		
		tx.setDate(this.datePicker.getValue().toString());
		tx.setDescription(this.memoArea.getText());
		tx.setToFrom(this.toFromField.getText());
		tx.setCheckNumber(this.checkNumberField.getText());
		tx.setClearBank(this.clearBankField.getText());
		tx.setBank(this.bankField.getText().isBlank() ?
			this.clearBankField.getText() : this.bankField.getText());
		tx.setReconciled(this.reconciledCheckBox.isSelected());
		tx.setBudgetTracking(this.budgetTrackingField.getText());
		tx.setAssociatedFundName(selectedFundName());
		tx.setInfo(info);
		tx.setSupplementalLines(collectSupplementalLines());
		
		this.onSave.accept(tx);
		
	}
	
	/**
	 * Load from transaction.
	 *
	 * @param tx the tx
	 */
	private void loadFromTransaction(AccountingTransaction tx)
	{
		this.datePicker.setValue(LocalDate.parse(tx.getDate()));
		this.memoArea.setText(
			tx.getDescription() != null ? tx.getDescription() : tx.getMemo());
		this.toFromField.setText(tx.getToFrom());
		this.checkNumberField.setText(tx.getCheckNumber());
		this.clearBankField.setText(tx.getClearBank());
		this.bankField.setText(tx.getBank());
		this.reconciledCheckBox.setSelected(tx.isReconciled());
		this.budgetTrackingField.setText(tx.getBudgetTracking());
		refreshFundChoices();
		selectFundName(tx.getAssociatedFundName());
		
		this.lines.clear();
		
		if (tx.getEntries() != null)
		{
			
			for (AccountingEntry entry : tx.getEntries())
			{
				Line line = new Line();
				Account account =
					this.chartOfAccounts.getAccount(entry.getAccountNumber());
				line.account.set(account != null ? account.getName() :
					entry.getAccountNumber());
				
				if (entry.getAccountSide() == AccountSide.DEBIT)
				{
					line.debit.set(entry.getAmount());
				}
				else
				{
					line.credit.set(entry.getAmount());
				}
				
				this.lines.add(line);
			}
			
		}
		
		if (this.lines.isEmpty())
		{
			addLine();
		}
		
		this.supplementalTabs.setEntryRefs(loadEntryRefs(tx.getId()));
		loadSupplementalLines(tx);
		updateSupplementalTabAvailability();
		
	}
	

	private void loadDonationInfo(Map<String, String> info)
	{
		boolean donation = info != null &&
			INFO_MODULE_DONATION.equalsIgnoreCase(info.get(INFO_MODULE));
		this.donationScheduleEnabled.setSelected(donation);
		if (!donation)
		{
			this.donationIdField.clear();
			selectDonor(null);
			updateDonationScheduleEnabled();
			return;
		}
		this.donationIdField.setText(info.getOrDefault(INFO_DONATION_ID, ""));
		refreshDonorChoices();
		selectDonor(info.get(INFO_DONOR_ID));
		updateDonationScheduleEnabled();
	}

	/**
	 * Resolve account.
	 *
	 * @param token the token
	 * @return the account
	 */
	private Account resolveAccount(String token)
	{
		
		if (token == null || token.isBlank())
		{
			return null;
		}
		
		Account byName = this.accountsByName.get(token);
		
		if (byName != null)
		{
			return byName;
		}
		
		return this.chartOfAccounts.getAccount(token);
		
	}
	
	/**
	 * Resolve chart of accounts.
	 *
	 * @return the chart of accounts
	 */
	private static ChartOfAccounts resolveChartOfAccounts()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null)
		{
			throw new IllegalStateException(
				"JournalEntryWorkspaceFX requires an open company");
		}
		
		ChartOfAccounts chart = company.getChartOfAccounts();
		
		if (chart == null)
		{
			throw new IllegalStateException(
				"Current company does not have a chart of accounts loaded");
		}
		
		return chart;
		
	}
	
	/**
	 * Builds the accounts by name.
	 *
	 * @param chart the chart
	 * @return the map
	 */
	private static
		Map<String, Account> buildAccountsByName(ChartOfAccounts chart)
	{
		return chart.createAccountNumberMap().asMap().values().stream().collect(
			Collectors.toMap(Account::getName, a -> a, (a, b) -> a,
				LinkedHashMap::new));
		
	}
	
	/**
	 * Amount or zero.
	 *
	 * @param value the value
	 * @return the big decimal
	 */
	private static BigDecimal amountOrZero(BigDecimal value)
	{
		return value != null ? value : BigDecimal.ZERO;
		
	}
	
}
