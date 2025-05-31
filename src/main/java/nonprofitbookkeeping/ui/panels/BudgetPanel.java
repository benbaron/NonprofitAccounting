
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
		this.txtBudgetName.setText(this.currentBudget.getBudgetName());
		this.spnFiscalYear.setValue(this.currentBudget.getFiscalYear());
		this.txtDescription
			.setText(this.currentBudget.getDescription() == null ? "" : this.currentBudget.getDescription());
		this.txtCurrency.setText(this.currentBudget.getCurrency());
		
		// Applicable Fund ComboBox
		if (this.currentBudget.getApplicableFundId() == null)
		{
			this.cmbApplicableFund.setSelectedItem("All Funds");
		}
		else
		{
			this.availableFunds.stream()
				.filter(f -> this.currentBudget.getApplicableFundId().equals(f.getFundId()))
				.findFirst()
				.ifPresentOrElse(
					fund -> this.cmbApplicableFund.setSelectedItem(fund.getName()),
					() -> this.cmbApplicableFund.setSelectedItem("All Funds") // Fallback
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
		this.txtBudgetName = new JTextField(20);
		this.spnFiscalYear =
			new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(this.spnFiscalYear, "#");
		this.spnFiscalYear.setEditor(editor);
		this.txtDescription = new JTextField(30);
		
		Vector<String> fundNames = new Vector<>();
		fundNames.add("All Funds");
		
		for (Fund fund : this.availableFunds)
		{
			fundNames.add(fund.getName());
		}
		
		this.cmbApplicableFund = new JComboBox<>(fundNames);
		this.txtCurrency = new JTextField(5);
		this.txtCurrency.setEditable(false);
		
		// Budget Lines Table
		// Ensure currentBudget.getBudgetLines() is not null if currentBudget could be
		// partially constructed
		if (this.currentBudget.getBudgetLines() == null)
		{
			this.currentBudget.setBudgetLines(new ArrayList<>());
		}
		
		this.budgetLineTableModel = new BudgetLineTableModel(this.currentBudget.getBudgetLines(),
			this.chartOfAccounts, this.availableFunds);
		this.tblBudgetLines = new JTable(this.budgetLineTableModel);
		
		// Setup JComboBox for Periodicity column
		JComboBox<Periodicity> periodicityComboBox = new JComboBox<>(Periodicity.values());
		TableColumn periodicityColumn = this.tblBudgetLines.getColumnModel().getColumn(2); // Assuming
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
		TableColumn lineFundColumn = this.tblBudgetLines.getColumnModel().getColumn(3); // Assuming index
																					// 3 for Line
																					// Fund
		lineFundColumn.setCellEditor(new DefaultCellEditor(lineFundComboBox));
		
		
		// Buttons
		this.btnAddLine = new JButton("Add Line");
		this.btnEditLine = new JButton("Edit Line");
		this.btnRemoveLine = new JButton("Remove Line");
		this.btnSaveBudget = new JButton("Save Budget");
		this.btnClose = new JButton("Close");
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
		// Adding table buttons above the main action buttons, typically below the table
		// For now, let's put it in a panel that goes above the main save/close buttons.
		
		JPanel pnlSouthOuter = new JPanel(new BorderLayout());
		pnlSouthOuter.add(pnlTableButtons, BorderLayout.NORTH);
		
		// Bottom Panel for Actions
		JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlActions.add(this.btnSaveBudget);
		pnlActions.add(this.btnClose);
		pnlSouthOuter.add(pnlActions, BorderLayout.SOUTH);
		
		add(pnlSouthOuter, BorderLayout.SOUTH);
	}
	
	private void attachListeners()
	{
		this.btnClose.addActionListener(e -> dispose());
		
		this.btnAddLine.addActionListener(this::actionAddLine);
		this.btnEditLine.addActionListener(this::actionEditLine);
		this.btnRemoveLine.addActionListener(this::actionRemoveLine);
		this.btnSaveBudget.addActionListener(this::actionSaveBudget);
		
		// Update currentBudget object when UI fields change
		this.txtBudgetName.addActionListener(e -> this.currentBudget.setBudgetName(this.txtBudgetName.getText()));
		this.txtBudgetName.addFocusListener(new java.awt.event.FocusAdapter()
		{
			public void focusLost(java.awt.event.FocusEvent evt)
			{
				BudgetPanel.this.currentBudget.setBudgetName(BudgetPanel.this.txtBudgetName.getText());
			}
			
		});
		this.spnFiscalYear.addChangeListener(
			e -> this.currentBudget.setFiscalYear((Integer) this.spnFiscalYear.getValue()));
		this.txtDescription
			.addActionListener(e -> this.currentBudget.setDescription(this.txtDescription.getText()));
		this.txtDescription.addFocusListener(new java.awt.event.FocusAdapter()
		{
			public void focusLost(java.awt.event.FocusEvent evt)
			{
				BudgetPanel.this.currentBudget.setDescription(BudgetPanel.this.txtDescription.getText());
			}
			
		});
		this.cmbApplicableFund.addActionListener(e -> {
			String selectedFundName = (String) this.cmbApplicableFund.getSelectedItem();
			
			if ("All Funds".equals(selectedFundName) || selectedFundName == null)
			{
				this.currentBudget.setApplicableFundId(null);
			}
			else
			{
				this.availableFunds.stream()
					.filter(f -> selectedFundName.equals(f.getName()))
					.findFirst()
					.ifPresent(fund -> this.currentBudget.setApplicableFundId(fund.getFundId()));
			}
			
		});
	}
	
	private void actionAddLine(ActionEvent e)
	{
		BudgetLineDialog dialog =
			new BudgetLineDialog(this, "Add Budget Line", this.chartOfAccounts, this.availableFunds, null);
		dialog.setVisible(true);
		
		if (dialog.isSaved())
		{
			BudgetLine newLine = dialog.getBudgetLine();
			
			if (newLine != null)
			{
				this.currentBudget.addBudgetLine(newLine);
				this.budgetLineTableModel.fireTableDataChanged();
			}
			
		}
		
	}
	
	private void actionEditLine(ActionEvent e)
	{
		int selectedRow = this.tblBudgetLines.getSelectedRow();
		
		if (selectedRow >= 0)
		{
			BudgetLine lineToEdit = this.budgetLineTableModel.getBudgetLineAt(selectedRow);
			BudgetLineDialog dialog = new BudgetLineDialog(this, "Edit Budget Line",
				this.chartOfAccounts, this.availableFunds, lineToEdit);
			dialog.setVisible(true);
			
			if (dialog.isSaved())
			{
				// The dialog modifies the lineToEdit object directly or returns a new one
				// For simplicity, assume dialog modifies the passed lineToEdit object
				this.budgetLineTableModel.fireTableRowsUpdated(selectedRow, selectedRow);
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
		int selectedRow = this.tblBudgetLines.getSelectedRow();
		
		if (selectedRow >= 0)
		{
			
			if (JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this line?",
				"Confirm Remove", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				this.budgetLineTableModel.removeRow(selectedRow);
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
		this.currentBudget.setBudgetName(this.txtBudgetName.getText());
		this.currentBudget.setFiscalYear((Integer) this.spnFiscalYear.getValue());
		this.currentBudget.setDescription(this.txtDescription.getText());
		String selectedFundName = (String) this.cmbApplicableFund.getSelectedItem();
		
		if ("All Funds".equals(selectedFundName) || selectedFundName == null)
		{
			this.currentBudget.setApplicableFundId(null);
		}
		else
		{
			this.availableFunds.stream()
				.filter(f -> selectedFundName.equals(f.getName()))
				.findFirst()
				.ifPresent(fund -> this.currentBudget.setApplicableFundId(fund.getFundId()));
		}
		// Currency is read-only from company profile
		
		// Update currentBudget from UI fields
		this.currentBudget.setBudgetName(this.txtBudgetName.getText());
		this.currentBudget.setFiscalYear((Integer) this.spnFiscalYear.getValue());
		this.currentBudget.setDescription(this.txtDescription.getText());
		String selectedFundName1 = (String) this.cmbApplicableFund.getSelectedItem();
		
		if ("All Funds".equals(selectedFundName1) || selectedFundName1 == null)
		{
			this.currentBudget.setApplicableFundId(null);
		}
		else
		{
			this.availableFunds.stream()
				.filter(f -> selectedFundName1.equals(f.getName()))
				.findFirst()
				.ifPresent(fund -> this.currentBudget.setApplicableFundId(fund.getFundId()));
		}
		// currentBudget.setBudgetLines() is already managed by the table model directly
		// modifying the list.
		
		try
		{
			List<Budget> allBudgets = this.budgetService.loadBudgets(this.companyDirectory);
			
			// Ensure budgetId is generated for new budgets before comparison
			if (this.currentBudget.getBudgetId() == null || this.currentBudget.getBudgetId().trim().isEmpty())
			{
				this.currentBudget.getBudgetId(); // This will trigger UUID generation if it's null.
			}
			
			boolean found = false;
			
			for (int i = 0; i < allBudgets.size(); i++)
			{
				
				if (this.currentBudget.getBudgetId().equals(allBudgets.get(i).getBudgetId()))
				{
					allBudgets.set(i, this.currentBudget); // Replace existing
					found = true;
					break;
				}
				
			}
			
			if (!found)
			{
				allBudgets.add(this.currentBudget); // Add new
			}
			
			this.budgetService.saveBudgets(allBudgets, this.companyDirectory);
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
