
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.service.BudgetService; // Added
import nonprofitbookkeeping.model.budget.Periodicity;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File; // Added
import java.io.IOException; // Added
import java.time.LocalDate; // Added for new budget year
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.function.BiConsumer; // Added for refactoring focus listeners

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Swing {@link JDialog} that provides a user interface for creating and editing {@link Budget} objects.
 * It allows users to define overall budget properties (name, fiscal year, description, associated fund, currency)
 * and manage individual {@link BudgetLine} items within that budget using a {@link JTable} backed by a
 * {@link BudgetLineTableModel}.
 * <p>
 * The dialog interacts with {@link ChartOfAccounts} to populate account choices, a list of {@link Fund}s
 * for fund selection, and a {@link BudgetService} to load and save budget data.
 * </p>
 */
public class BudgetPanel extends JDialog
{
        private static final Logger LOGGER = LoggerFactory.getLogger(BudgetPanel.class);

        /** The current {@link Budget} object being created or edited in this dialog. */
	private Budget currentBudget;
	/** The {@link ChartOfAccounts} instance used to provide account information for budget lines. */
	private ChartOfAccounts chartOfAccounts;
	/** A list of available {@link Fund}s for selection as the budget's applicable fund or for line items. */
	private List<Fund> availableFunds;
	/** The {@link BudgetService} used for loading and saving budget data. */
	private BudgetService budgetService;
	/** The directory of the current company, used for saving/loading budget files. */
	private File companyDirectory;
	
	/** TextField for the budget's name. */
	private JTextField txtBudgetName;
	/** Spinner for selecting the budget's fiscal year. */
	private JSpinner spnFiscalYear;
	/** TextField for the budget's description. */
	private JTextField txtDescription;
	/** ComboBox for selecting a fund applicable to the entire budget, or "All Funds". */
	private JComboBox<String> cmbApplicableFund;
	/** TextField to display the budget's currency (usually non-editable). */
	private JTextField txtCurrency;
	
	/** JTable to display and manage individual budget lines. */
        private JTable tblBudgetLines;
        /** The {@link BudgetLineTableModel} that backs the {@code tblBudgetLines}. */
        private BudgetLineTableModel budgetLineTableModel;
	
	/** Button to add a new budget line. */
	private JButton btnAddLine;
	/** Button to edit the selected budget line. */
	private JButton btnEditLine;
	/** Button to remove the selected budget line. */
	private JButton btnRemoveLine;
	/** Button to save the current budget (new or edited). */
	private JButton btnSaveBudget;
	/** Button to close the dialog without saving. */
	private JButton btnClose;
	
	/**
	 * Constructs a new {@code BudgetPanel} dialog.
	 *
	 * @param owner The parent {@link Frame} that owns this dialog.
	 * @param chartOfAccounts The {@link ChartOfAccounts} for the current company, used for populating account choices. Must not be null.
	 * @param funds A list of available {@link Fund}s for populating fund selection choices. If null, an empty list is used.
	 * @param budgetService The {@link BudgetService} used for saving and loading budget data. Must not be null.
	 * @param companyDirectory The {@link File} representing the current company's data directory, used to locate budget files. Must not be null.
	 * @param budgetToEdit The {@link Budget} object to edit. If null, the dialog is configured for creating a new budget.
	 * @throws NullPointerException if {@code chartOfAccounts}, {@code budgetService}, or {@code companyDirectory} is null.
	 */
	public BudgetPanel(Frame owner, ChartOfAccounts chartOfAccounts, List<Fund> funds,
		BudgetService budgetService, File companyDirectory, Budget budgetToEdit)
	{
		super(owner, "Budget Editor", true); // Modal dialog
		this.chartOfAccounts =
			Objects.requireNonNull(chartOfAccounts, "ChartOfAccounts cannot be null.");
		this.availableFunds = (funds != null) ? funds : new ArrayList<>();
		this.budgetService = Objects.requireNonNull(budgetService, "BudgetService cannot be null.");
		this.companyDirectory =
			Objects.requireNonNull(companyDirectory, "CompanyDirectory cannot be null.");
		
		if (budgetToEdit != null)
		{
			this.currentBudget = budgetToEdit;
		}
		else
		{
			this.currentBudget = new Budget("New Budget", LocalDate.now().getYear());
			
			// Attempt to set default currency from current company profile
			if (CurrentCompany.getCompany() != null &&
				CurrentCompany.getCompany().getCompanyProfile() != null)
			{
				String baseCurrency =
					CurrentCompany.getCompany().getCompanyProfile().getBaseCurrency();
				this.currentBudget
					.setCurrency(
						(baseCurrency != null && !baseCurrency.isEmpty()) ? baseCurrency : "USD");
			}
			else
			{
				this.currentBudget.setCurrency("USD"); // Default if company context not available
			}
			
		}
		
		initComponents(); // Initializes UI components
		populateUIFromCurrentBudget(); // Populates UI with currentBudget data
		layoutComponents();
		attachListeners();
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(owner);
	}
	
	/**
	 * Populates the UI fields of the dialog with data from the {@link #currentBudget} object.
	 * This method is called during initialization to set initial values for budget name,
	 * fiscal year, description, currency, and the applicable fund.
	 * It also ensures the budget line table reflects the lines in the current budget.
	 */
	private void populateUIFromCurrentBudget()
	{
		this.txtBudgetName.setText(this.currentBudget.getBudgetName());
		this.spnFiscalYear.setValue(this.currentBudget.getFiscalYear());
		this.txtDescription
			.setText(this.currentBudget.getDescription() == null ? "" :
				this.currentBudget.getDescription());
		this.txtCurrency.setText(this.currentBudget.getCurrency());
		
		// Applicable Fund ComboBox
		if (this.currentBudget.getApplicableFundId() == null)
		{
			this.cmbApplicableFund.setSelectedItem("All Funds");
		}
		else
		{
			this.availableFunds.stream()
				.filter(f -> f != null &&
					this.currentBudget.getApplicableFundId().equals(f.getFundId())) // Added null
																					// check for f
				.findFirst()
				.ifPresentOrElse(
					fund -> this.cmbApplicableFund.setSelectedItem(fund.getName()),
					() -> this.cmbApplicableFund.setSelectedItem("All Funds") // Fallback if fund ID
																				// not found
				);
		}
		
		// The BudgetLineTableModel is already initialized with
		// currentBudget.getBudgetLines()
		// in initComponents, so it will reflect the current budget's lines.
		// If budgetLines could be null initially in currentBudget, ensure it's handled:
		if (this.currentBudget.getBudgetLines() == null)
		{
			this.currentBudget.setBudgetLines(new ArrayList<>());
		}
		
		// Re-assign or update model if necessary, though current setup in
		// initComponents should handle it.
		this.budgetLineTableModel = new BudgetLineTableModel(this.currentBudget.getBudgetLines(),
			this.chartOfAccounts, this.availableFunds);
		this.tblBudgetLines.setModel(this.budgetLineTableModel);
	}
	
	private Vector<String> createFundNameVector(String initialElement) {
		Vector<String> fundNames = new Vector<>();
		if (initialElement != null) {
			fundNames.add(initialElement);
		}
		if (this.availableFunds != null) {
			this.availableFunds.stream()
				.filter(Objects::nonNull)
				.map(Fund::getName)
				.filter(Objects::nonNull)
				.forEach(fundNames::add);
		}
		return fundNames;
	}

	/**
	 * Initializes all UI components for the dialog, including text fields, spinners,
	 * combo boxes, table, and buttons. It also sets up cell editors for specific
	 * columns in the budget lines table (Periodicity and Line Fund).
	 */
	private void initComponents()
	{
		// Budget Properties - Initialize them first, then populate in
		// populateUIFromCurrentBudget
		this.txtBudgetName = new JTextField(20);
		this.spnFiscalYear =
			new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(this.spnFiscalYear, "#"); // No
																							// decimal
																							// for
																							// year
		this.spnFiscalYear.setEditor(editor);
		this.txtDescription = new JTextField(30);
		
		this.cmbApplicableFund = new JComboBox<>(createFundNameVector("All Funds"));
		this.txtCurrency = new JTextField(5);
		this.txtCurrency.setEditable(false); // Currency is usually derived
		
		// Budget Lines Table
		// Ensure currentBudget.getBudgetLines() is not null
		if (this.currentBudget.getBudgetLines() == null)
		{
			this.currentBudget.setBudgetLines(new ArrayList<>());
		}
		
		this.budgetLineTableModel = new BudgetLineTableModel(this.currentBudget.getBudgetLines(),
			this.chartOfAccounts, this.availableFunds);
                this.tblBudgetLines = new JTable(this.budgetLineTableModel);
                this.tblBudgetLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Setup JComboBox for Periodicity column in the table
		JComboBox<Periodicity> periodicityComboBox = new JComboBox<>(Periodicity.values());
		TableColumn periodicityColumn = this.tblBudgetLines.getColumnModel().getColumn(2); // Column
																							// index
																							// 2 for
																							// Periodicity
		periodicityColumn.setCellEditor(new DefaultCellEditor(periodicityComboBox));
		
		// Setup JComboBox for Fund column (line-specific fund) in the table
		JComboBox<String> lineFundComboBox = new JComboBox<>(createFundNameVector("None"));
		TableColumn lineFundColumn = this.tblBudgetLines.getColumnModel().getColumn(3); // Column
																						// index 3
																						// for Line
																						// Fund
		lineFundColumn.setCellEditor(new DefaultCellEditor(lineFundComboBox));
		
		
		// Buttons
                this.btnAddLine = new JButton("Add Line");
                this.btnEditLine = new JButton("Edit Line");
                this.btnRemoveLine = new JButton("Remove Line");
                this.btnEditLine.setEnabled(false);
                this.btnRemoveLine.setEnabled(false);
		this.btnSaveBudget = new JButton("Save Budget");
		this.btnClose = new JButton("Close");
	}
	
	/**
	 * Lays out the UI components on the dialog panel using {@link BorderLayout} and {@link GridBagLayout}.
	 * Budget properties are placed at the top (NORTH), the budget lines table in the center (CENTER),
	 * and action buttons at the bottom (SOUTH).
	 */
	private void layoutComponents()
	{
		setLayout(new BorderLayout(5, 5));
		
		// Top Panel for Budget Properties
		JPanel pnlProperties = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.WEST;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		pnlProperties.add(new JLabel("Budget Name:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(this.txtBudgetName, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		pnlProperties.add(new JLabel("Fiscal Year:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(this.spnFiscalYear, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		pnlProperties.add(new JLabel("Description:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(this.txtDescription, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		pnlProperties.add(new JLabel("Applicable Fund:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(this.cmbApplicableFund, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		pnlProperties.add(new JLabel("Currency:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(this.txtCurrency, gbc);
		
		add(pnlProperties, BorderLayout.NORTH);
		
		// Middle Panel for Budget Lines Table
		JScrollPane scrollPane = new JScrollPane(this.tblBudgetLines);
		add(scrollPane, BorderLayout.CENTER);
		
		// Button Panel for Table Management (could be part of Middle or Bottom)
		JPanel pnlTableButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlTableButtons.add(this.btnAddLine);
		pnlTableButtons.add(this.btnEditLine);
		pnlTableButtons.add(this.btnRemoveLine);
		
		JPanel pnlSouthOuter = new JPanel(new BorderLayout());
		pnlSouthOuter.add(pnlTableButtons, BorderLayout.NORTH); // Table buttons above main action
																// buttons
		
		// Bottom Panel for Actions
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlActions.add(this.btnSaveBudget);
		pnlActions.add(this.btnClose);
		pnlSouthOuter.add(pnlActions, BorderLayout.SOUTH);
		
		add(pnlSouthOuter, BorderLayout.SOUTH);
	}
	
	/**
	 * Attaches action listeners to the dialog's buttons and change listeners to input fields
	 * to update the {@link #currentBudget} object dynamically as the user interacts with the UI.
	 */
        private void updateCurrentBudgetApplicableFund() {
                String selectedFundName = (String) this.cmbApplicableFund.getSelectedItem();
                if ("All Funds".equals(selectedFundName) || selectedFundName == null) {
                        this.currentBudget.setApplicableFundId(null);
                } else {
                        this.availableFunds.stream()
                                .filter(f -> f != null && selectedFundName.equals(f.getName())) // Add null check for fund
                                .findFirst()
                                .ifPresent(fund -> this.currentBudget.setApplicableFundId(fund.getFundId()));
                }
        }

       /**
        * Factory method for creating {@link BudgetLineDialog} instances. This allows
        * tests to override dialog creation and provide stub implementations without
        * displaying a Swing UI.
        *
        * @param title       the title to use for the dialog
        * @param existingLine an existing {@link BudgetLine} to edit, or {@code null}
        *                     when creating a new line
        * @return a new {@link BudgetLineDialog}
        */
       protected BudgetLineDialog createBudgetLineDialog(String title, BudgetLine existingLine)
       {
               return new BudgetLineDialog(this, title, this.chartOfAccounts, this.availableFunds, existingLine);
       }

        private void attachListeners()
        {
                this.btnClose.addActionListener(e -> dispose());

                this.btnAddLine.addActionListener(this::actionAddLine);
                this.btnEditLine.addActionListener(this::actionEditLine);
                this.btnRemoveLine.addActionListener(this::actionRemoveLine);
                this.btnSaveBudget.addActionListener(this::actionSaveBudget);

                this.tblBudgetLines.getSelectionModel().addListSelectionListener(event ->
                {
                        if (!event.getValueIsAdjusting())
                        {
                                updateLineActionButtons();
                        }
                });
		
		// Helper for focus listeners
		BiConsumer<JTextField, BiConsumer<Budget, String>> addBudgetUpdateFocusListener =
			(textField, setter) -> textField.addFocusListener(new java.awt.event.FocusAdapter() {
				@Override
				public void focusLost(java.awt.event.FocusEvent evt) {
					setter.accept(BudgetPanel.this.currentBudget, textField.getText());
				}
			});

		// Update currentBudget object when UI fields change
		this.txtBudgetName
			.addActionListener(e -> this.currentBudget.setBudgetName(this.txtBudgetName.getText()));
		addBudgetUpdateFocusListener.accept(this.txtBudgetName, Budget::setBudgetName);

		this.spnFiscalYear.addChangeListener(
			e -> this.currentBudget.setFiscalYear((Integer) this.spnFiscalYear.getValue()));

		this.txtDescription
			.addActionListener(
				e -> this.currentBudget.setDescription(this.txtDescription.getText()));
		addBudgetUpdateFocusListener.accept(this.txtDescription, Budget::setDescription);

                this.cmbApplicableFund.addActionListener(e -> updateCurrentBudgetApplicableFund());

                updateLineActionButtons();
        }

        private void updateLineActionButtons()
        {
                boolean hasSelection = this.tblBudgetLines.getSelectedRow() >= 0;
                this.btnEditLine.setEnabled(hasSelection);
                this.btnRemoveLine.setEnabled(hasSelection);
        }
	
	/**
	 * Handles the action of adding a new budget line.
	 * Opens a {@link BudgetLineDialog} to gather details for the new line.
	 * If the user saves the new line, it's added to the {@link #currentBudget}
	 * and the budget lines table is updated.
	 *
	 * @param e The {@link ActionEvent} that triggered this action.
	 */
	private void actionAddLine(ActionEvent e)
	{
		BudgetLineDialog dialog = createBudgetLineDialog("Add Budget Line", null);
		dialog.setVisible(true);

		if (dialog.isSaved())
		{
			BudgetLine newLine = dialog.getBudgetLine();

			if (newLine != null)
			{
				this.budgetLineTableModel.addRow(newLine);
				int newIndex = this.budgetLineTableModel.getRowCount() - 1;

				if (newIndex >= 0)
				{
					this.tblBudgetLines.getSelectionModel().setSelectionInterval(newIndex, newIndex);
					this.tblBudgetLines.requestFocusInWindow();
				}
			}
			else
			{
				LOGGER.warn("BudgetLineDialog reported success but returned no budget line instance.");
			}

			updateLineActionButtons();
		}

	}

	/**
	 * Handles the action of editing an existing budget line.
	 * Opens a {@link BudgetLineDialog} pre-populated with the data of the selected line from the table.
	 * If the user saves the changes, the budget lines table is updated to reflect the modifications.
	 *
	 * @param e The {@link ActionEvent} that triggered this action.
	 */
	private void actionEditLine(ActionEvent e)
	{
		int selectedRow = this.tblBudgetLines.getSelectedRow();

		if (selectedRow >= 0)
		{
			BudgetLine lineToEdit = this.budgetLineTableModel.getBudgetLineAt(selectedRow);

			if (lineToEdit == null)
			{ // Should not happen if row is selected, but defensive
				JOptionPane.showMessageDialog(this, "Selected line data is not available.", "Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}

			BudgetLineDialog dialog = createBudgetLineDialog("Edit Budget Line", lineToEdit);
			dialog.setVisible(true);

			if (dialog.isSaved())
			{
				BudgetLine editedLine = dialog.getBudgetLine();

				if (editedLine == null)
				{
					LOGGER.warn("BudgetLineDialog returned null after editing row {}.", selectedRow);
					editedLine = lineToEdit;
				}

				if (editedLine != lineToEdit)
				{
					this.currentBudget.getBudgetLines().set(selectedRow, editedLine);
				}

				this.budgetLineTableModel.fireTableRowsUpdated(selectedRow, selectedRow);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Please select a line to edit.", "No Selection",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Handles the action of removing a selected budget line from the table.
	 * Prompts the user for confirmation before removing the line.
	 * If confirmed, the line is removed from the {@link #currentBudget} and the table is updated.
	 *
	 * @param e The {@link ActionEvent} that triggered this action.
	 */
		private void actionRemoveLine(ActionEvent e)
	{
		int selectedRow = this.tblBudgetLines.getSelectedRow();

		if (selectedRow >= 0)
		{
			if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this line?",
				"Confirm Remove", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				this.budgetLineTableModel.removeRow(selectedRow); // This modifies
				int remaining = this.budgetLineTableModel.getRowCount();
				if (remaining > 0)
				{
					int nextSelection = Math.min(selectedRow, remaining - 1);
					this.tblBudgetLines.getSelectionModel().setSelectionInterval(nextSelection, nextSelection);
				}
				else
				{
					this.tblBudgetLines.clearSelection();
				}

				updateLineActionButtons();
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Please select a line to remove.", "No Selection",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Handles the action of saving the current budget (new or edited).
	 * It first updates the {@link #currentBudget} object with the latest values from all UI fields.
	 * Then, it uses the {@link BudgetService} to load all existing budgets, replaces the
	 * current budget if it already exists (by ID) or adds it as a new budget, and then
	 * saves the entire list of budgets back.
	 * Displays success or error messages using {@link JOptionPane}.
	 *
	 * @param e The {@link ActionEvent} that triggered this action.
	 */
	private void actionSaveBudget(ActionEvent e)
	{
		// Update budget properties from UI just before saving
		this.currentBudget.setBudgetName(this.txtBudgetName.getText().trim());
		this.currentBudget.setFiscalYear((Integer) this.spnFiscalYear.getValue());
		this.currentBudget.setDescription(this.txtDescription.getText().trim());
		updateCurrentBudgetApplicableFund();
		// Currency is typically read-only, derived from company profile, so no set
		// here.
		
		// Note: currentBudget.getBudgetLines() is already up-to-date due to
		// BudgetLineTableModel
		// directly operating on this list instance.
		
		try
		{
			List<Budget> allBudgets = this.budgetService.loadBudgets(this.companyDirectory);
			if (allBudgets == null)
				allBudgets = new ArrayList<>(); // Ensure list is not null
				
			// Ensure budgetId is generated for new budgets before comparison
			// The getBudgetId() in Budget model should handle UUID generation if null.
			String currentBudgetId = this.currentBudget.getBudgetId();
			
			boolean found = false;
			
			if (currentBudgetId != null && !currentBudgetId.trim().isEmpty())
			{ // Only try to replace if ID exists
				
				for (int i = 0; i < allBudgets.size(); i++)
				{
					Budget existing = allBudgets.get(i);
					
					if (existing != null && currentBudgetId.equals(existing.getBudgetId()))
					{
						allBudgets.set(i, this.currentBudget); // Replace existing
						found = true;
						break;
					}
					
				}
				
			}
			
			if (!found)
			{
				allBudgets.add(this.currentBudget); // Add as new if not found or if ID was new
			}
			
			BudgetService.saveBudgets(allBudgets, this.companyDirectory);
			JOptionPane.showMessageDialog(this, "Budget saved successfully!", "Success",
				JOptionPane.INFORMATION_MESSAGE);
			// Optionally, dispose or indicate saved state
			// dispose(); // If save implies close for this dialog
		}
		catch (IOException ex)
		{
			ex.printStackTrace(); // Consider more robust logging
			JOptionPane.showMessageDialog(this, "Error saving budget: " + ex.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	/**
	 * Main method for testing the {@code BudgetPanel} dialog independently.
	 * Note: This method requires adaptation to properly instantiate and provide
	 * dependencies like {@link ChartOfAccounts}, {@link Fund} list, {@link BudgetService},
	 * and a company directory {@link File}.
	 * The current implementation only prints a message about needed adaptations.
	 *
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args)
	{
		// This main method needs to be updated to provide BudgetService and File
		// companyDirectory.
		// For basic UI testing, it can be simplified or dependencies can be
		// mocked/faked.
		// Example:
		// ChartOfAccounts coa = new ChartOfAccounts(); // ... populate coa ...
		// List<Fund> funds = new ArrayList<>(); // ... populate funds ...
		// BudgetService mockBudgetService = new BudgetService(); // Or a mock
		// File mockCompanyDir = new File("."); // Placeholder
		// Budget budgetToEdit = null; // For new budget
		
		// if (CurrentCompany.getCompany() == null) {
		// nonprofitbookkeeping.model.Company testCompany = new
		// nonprofitbookkeeping.model.Company();
		// nonprofitbookkeeping.model.CompanyProfileModel profile = new
		// nonprofitbookkeeping.model.CompanyProfileModel();
		// profile.setCurrency("CAD");
		// testCompany.setCompanyProfileModel(profile);
		// CurrentCompany.setCompany(testCompany);
		// }
		
		// SwingUtilities.invokeLater(() -> {
		// BudgetPanel panel = new BudgetPanel(null, coa, funds, mockBudgetService,
		// mockCompanyDir, budgetToEdit);
		// panel.setVisible(true);
		// });
                LOGGER.info(
                        "BudgetPanel main method needs adaptation for new constructor dependencies (BudgetService, companyDirectory).");
        }
	
}
