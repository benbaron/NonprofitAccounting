
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.Account;
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
import java.math.BigDecimal;
import java.io.File; // Added
import java.io.IOException; // Added
import java.time.LocalDate; // Added for new budget year
import java.util.ArrayList;
import java.util.Calendar; // Keep for default year if needed, or use LocalDate
import java.util.List;
import java.util.Vector;

public class BudgetPanel extends JDialog
{
	private Budget currentBudget;
	private ChartOfAccounts chartOfAccounts;
	private List<Fund> availableFunds;
	private BudgetService budgetService; // Added
	private File companyDirectory; // Added
	
	private JTextField txtBudgetName;
	private JSpinner spnFiscalYear;
	private JTextField txtDescription;
	private JComboBox<String> cmbApplicableFund;
	private JTextField txtCurrency;
	
	private JTable tblBudgetLines;
	private BudgetLineTableModel budgetLineTableModel;
	
	private JButton btnAddLine;
	private JButton btnEditLine;
	private JButton btnRemoveLine;
	private JButton btnSaveBudget;
	private JButton btnClose;
	
	public BudgetPanel(Frame owner, ChartOfAccounts chartOfAccounts, List<Fund> funds,
		BudgetService budgetService, File companyDirectory, Budget budgetToEdit)
	{
		super(owner, "Budget Editor", true);
		this.chartOfAccounts = chartOfAccounts;
		this.availableFunds = (funds != null) ? funds : new ArrayList<>();
		this.budgetService = budgetService;
		this.companyDirectory = companyDirectory;
		
		if (budgetToEdit != null)
		{
			this.currentBudget = budgetToEdit;
		}
		else
		{
			this.currentBudget = new Budget("New Budget", LocalDate.now().getYear());
			
			if (CurrentCompany.getCompany() != null &&
				CurrentCompany.getCompany().getCompanyProfile() != null)
			{
				this.currentBudget
					.setCurrency(CurrentCompany.getCompany().getCompanyProfile().getBaseCurrency());
			}
			else
			{
				this.currentBudget.setCurrency("USD"); // Default
			}
			
		}
		
		initComponents(); // Initializes UI components
		populateUIFromCurrentBudget(); // Populates UI with currentBudget data
		layoutComponents();
		attachListeners();
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(owner);
	}
	
	private void populateUIFromCurrentBudget()
	{
		txtBudgetName.setText(currentBudget.getBudgetName());
		spnFiscalYear.setValue(currentBudget.getFiscalYear());
		txtDescription
			.setText(currentBudget.getDescription() == null ? "" : currentBudget.getDescription());
		txtCurrency.setText(currentBudget.getCurrency());
		
		// Applicable Fund ComboBox
		if (currentBudget.getApplicableFundId() == null)
		{
			cmbApplicableFund.setSelectedItem("All Funds");
		}
		else
		{
			availableFunds.stream()
				.filter(f -> currentBudget.getApplicableFundId().equals(f.getFundId()))
				.findFirst()
				.ifPresentOrElse(
					fund -> cmbApplicableFund.setSelectedItem(fund.getName()),
					() -> cmbApplicableFund.setSelectedItem("All Funds") // Fallback
				);
		}
		
		// The BudgetLineTableModel is already initialized with
		// currentBudget.getBudgetLines()
		// in initComponents, so it will reflect the current budget's lines.
		// If not, we would need: budgetLineTableModel = new BudgetLineTableModel(...);
		// tblBudgetLines.setModel(...);
	}
	
	private void initComponents()
	{
		// Budget Properties - Initialize them first, then populate in
		// populateUIFromCurrentBudget
		txtBudgetName = new JTextField(20);
		spnFiscalYear =
			new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spnFiscalYear, "#");
		spnFiscalYear.setEditor(editor);
		txtDescription = new JTextField(30);
		
		Vector<String> fundNames = new Vector<>();
		fundNames.add("All Funds");
		
		for (Fund fund : availableFunds)
		{
			fundNames.add(fund.getName());
		}
		
		cmbApplicableFund = new JComboBox<>(fundNames);
		txtCurrency = new JTextField(5);
		txtCurrency.setEditable(false);
		
		// Budget Lines Table
		// Ensure currentBudget.getBudgetLines() is not null if currentBudget could be
		// partially constructed
		if (currentBudget.getBudgetLines() == null)
		{
			currentBudget.setBudgetLines(new ArrayList<>());
		}
		
		budgetLineTableModel = new BudgetLineTableModel(currentBudget.getBudgetLines(),
			chartOfAccounts, availableFunds);
		tblBudgetLines = new JTable(budgetLineTableModel);
		
		// Setup JComboBox for Periodicity column
		JComboBox<Periodicity> periodicityComboBox = new JComboBox<>(Periodicity.values());
		TableColumn periodicityColumn = tblBudgetLines.getColumnModel().getColumn(2); // Assuming
																						// index 2
																						// for
																						// Periodicity
		periodicityColumn.setCellEditor(new DefaultCellEditor(periodicityComboBox));
		
		// Setup JComboBox for Fund column (line-specific fund)
		// Using fundNames vector which includes "All Funds" or a "None" option if
		// appropriate
		Vector<String> lineFundNames = new Vector<>(fundNames); // Copy, can be modified if needed
		if (!lineFundNames.contains("None"))
			lineFundNames.add(0, "None"); // Option for no specific fund on a line
		JComboBox<String> lineFundComboBox = new JComboBox<>(lineFundNames);
		TableColumn lineFundColumn = tblBudgetLines.getColumnModel().getColumn(3); // Assuming index
																					// 3 for Line
																					// Fund
		lineFundColumn.setCellEditor(new DefaultCellEditor(lineFundComboBox));
		
		
		// Buttons
		btnAddLine = new JButton("Add Line");
		btnEditLine = new JButton("Edit Line");
		btnRemoveLine = new JButton("Remove Line");
		btnSaveBudget = new JButton("Save Budget");
		btnClose = new JButton("Close");
	}
	
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
		pnlProperties.add(txtBudgetName, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		pnlProperties.add(new JLabel("Fiscal Year:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(spnFiscalYear, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		pnlProperties.add(new JLabel("Description:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(txtDescription, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		pnlProperties.add(new JLabel("Applicable Fund:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(cmbApplicableFund, gbc);
		gbc.gridx = 0;
		gbc.gridy = 4;
		pnlProperties.add(new JLabel("Currency:"), gbc);
		gbc.gridx = 1;
		pnlProperties.add(txtCurrency, gbc);
		
		add(pnlProperties, BorderLayout.NORTH);
		
		// Middle Panel for Budget Lines Table
		JScrollPane scrollPane = new JScrollPane(tblBudgetLines);
		add(scrollPane, BorderLayout.CENTER);
		
		// Button Panel for Table Management (could be part of Middle or Bottom)
		JPanel pnlTableButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlTableButtons.add(btnAddLine);
		pnlTableButtons.add(btnEditLine);
		pnlTableButtons.add(btnRemoveLine);
		// Adding table buttons above the main action buttons, typically below the table
		// For now, let's put it in a panel that goes above the main save/close buttons.
		
		JPanel pnlSouthOuter = new JPanel(new BorderLayout());
		pnlSouthOuter.add(pnlTableButtons, BorderLayout.NORTH);
		
		// Bottom Panel for Actions
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlActions.add(btnSaveBudget);
		pnlActions.add(btnClose);
		pnlSouthOuter.add(pnlActions, BorderLayout.SOUTH);
		
		add(pnlSouthOuter, BorderLayout.SOUTH);
	}
	
	private void attachListeners()
	{
		btnClose.addActionListener(e -> dispose());
		
		btnAddLine.addActionListener(this::actionAddLine);
		btnEditLine.addActionListener(this::actionEditLine);
		btnRemoveLine.addActionListener(this::actionRemoveLine);
		btnSaveBudget.addActionListener(this::actionSaveBudget);
		
		// Update currentBudget object when UI fields change
		txtBudgetName.addActionListener(e -> currentBudget.setBudgetName(txtBudgetName.getText()));
		txtBudgetName.addFocusListener(new java.awt.event.FocusAdapter()
		{
			public void focusLost(java.awt.event.FocusEvent evt)
			{
				currentBudget.setBudgetName(txtBudgetName.getText());
			}
			
		});
		spnFiscalYear.addChangeListener(
			e -> currentBudget.setFiscalYear((Integer) spnFiscalYear.getValue()));
		txtDescription
			.addActionListener(e -> currentBudget.setDescription(txtDescription.getText()));
		txtDescription.addFocusListener(new java.awt.event.FocusAdapter()
		{
			public void focusLost(java.awt.event.FocusEvent evt)
			{
				currentBudget.setDescription(txtDescription.getText());
			}
			
		});
		cmbApplicableFund.addActionListener(e -> {
			String selectedFundName = (String) cmbApplicableFund.getSelectedItem();
			
			if ("All Funds".equals(selectedFundName) || selectedFundName == null)
			{
				currentBudget.setApplicableFundId(null);
			}
			else
			{
				availableFunds.stream()
					.filter(f -> selectedFundName.equals(f.getName()))
					.findFirst()
					.ifPresent(fund -> currentBudget.setApplicableFundId(fund.getFundId()));
			}
			
		});
	}
	
	private void actionAddLine(ActionEvent e)
	{
		BudgetLineDialog dialog =
			new BudgetLineDialog(this, "Add Budget Line", chartOfAccounts, availableFunds, null);
		dialog.setVisible(true);
		
		if (dialog.isSaved())
		{
			BudgetLine newLine = dialog.getBudgetLine();
			
			if (newLine != null)
			{
				currentBudget.addBudgetLine(newLine);
				budgetLineTableModel.fireTableDataChanged();
			}
			
		}
		
	}
	
	private void actionEditLine(ActionEvent e)
	{
		int selectedRow = tblBudgetLines.getSelectedRow();
		
		if (selectedRow >= 0)
		{
			BudgetLine lineToEdit = budgetLineTableModel.getBudgetLineAt(selectedRow);
			BudgetLineDialog dialog = new BudgetLineDialog(this, "Edit Budget Line",
				chartOfAccounts, availableFunds, lineToEdit);
			dialog.setVisible(true);
			
			if (dialog.isSaved())
			{
				// The dialog modifies the lineToEdit object directly or returns a new one
				// For simplicity, assume dialog modifies the passed lineToEdit object
				budgetLineTableModel.fireTableRowsUpdated(selectedRow, selectedRow);
			}
			
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Please select a line to edit.", "No Selection",
				JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	private void actionRemoveLine(ActionEvent e)
	{
		int selectedRow = tblBudgetLines.getSelectedRow();
		
		if (selectedRow >= 0)
		{
			
			if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this line?",
				"Confirm Remove", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				budgetLineTableModel.removeRow(selectedRow);
				// currentBudget.getBudgetLines() is directly modified by table model
			}
			
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Please select a line to remove.", "No Selection",
				JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	private void actionSaveBudget(ActionEvent e)
	{
		// Update budget properties from UI just before saving
		currentBudget.setBudgetName(txtBudgetName.getText());
		currentBudget.setFiscalYear((Integer) spnFiscalYear.getValue());
		currentBudget.setDescription(txtDescription.getText());
		String selectedFundName = (String) cmbApplicableFund.getSelectedItem();
		
		if ("All Funds".equals(selectedFundName) || selectedFundName == null)
		{
			currentBudget.setApplicableFundId(null);
		}
		else
		{
			availableFunds.stream()
				.filter(f -> selectedFundName.equals(f.getName()))
				.findFirst()
				.ifPresent(fund -> currentBudget.setApplicableFundId(fund.getFundId()));
		}
		// Currency is read-only from company profile
		
		// Update currentBudget from UI fields
		currentBudget.setBudgetName(txtBudgetName.getText());
		currentBudget.setFiscalYear((Integer) spnFiscalYear.getValue());
		currentBudget.setDescription(txtDescription.getText());
		String selectedFundName1 = (String) cmbApplicableFund.getSelectedItem();
		
		if ("All Funds".equals(selectedFundName1) || selectedFundName1 == null)
		{
			currentBudget.setApplicableFundId(null);
		}
		else
		{
			availableFunds.stream()
				.filter(f -> selectedFundName1.equals(f.getName()))
				.findFirst()
				.ifPresent(fund -> currentBudget.setApplicableFundId(fund.getFundId()));
		}
		// currentBudget.setBudgetLines() is already managed by the table model directly
		// modifying the list.
		
		try
		{
			List<Budget> allBudgets = budgetService.loadBudgets(companyDirectory);
			
			// Ensure budgetId is generated for new budgets before comparison
			if (currentBudget.getBudgetId() == null || currentBudget.getBudgetId().trim().isEmpty())
			{
				currentBudget.getBudgetId(); // This will trigger UUID generation if it's null.
			}
			
			boolean found = false;
			
			for (int i = 0; i < allBudgets.size(); i++)
			{
				
				if (currentBudget.getBudgetId().equals(allBudgets.get(i).getBudgetId()))
				{
					allBudgets.set(i, currentBudget); // Replace existing
					found = true;
					break;
				}
				
			}
			
			if (!found)
			{
				allBudgets.add(currentBudget); // Add new
			}
			
			budgetService.saveBudgets(allBudgets, companyDirectory);
			JOptionPane.showMessageDialog(this, "Budget saved successfully!", "Success",
				JOptionPane.INFORMATION_MESSAGE);
			// Optionally, dispose or indicate saved state
			// dispose(); // If save implies close for this dialog
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error saving budget: " + ex.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	// Main method for testing (optional, needs adaptation for new constructor)
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
		System.out.println(
			"BudgetPanel main method needs adaptation for new constructor dependencies (BudgetService, companyDirectory).");
	}
	
}
