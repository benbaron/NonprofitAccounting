
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.List;

public class BudgetLineTableModel extends AbstractTableModel
{
	private final List<BudgetLine> budgetLines;
	private final ChartOfAccounts chartOfAccounts; // To resolve account names
	private final List<Fund> availableFunds; // To resolve fund names
	
	private final String[] columnNames =
	{ "Account Name", "Total Budgeted Amount", "Periodicity", "Line Fund" };
	
	public BudgetLineTableModel(List<BudgetLine> budgetLines, ChartOfAccounts chartOfAccounts,
		List<Fund> availableFunds)
	{
		this.budgetLines = budgetLines;
		this.chartOfAccounts = chartOfAccounts;
		this.availableFunds = availableFunds != null ? availableFunds : List.of();
	}
	
	@Override public int getRowCount()
	{
		return this.budgetLines.size();
	}
	
	@Override public int getColumnCount()
	{
		return this.columnNames.length;
	}
	
	@Override public String getColumnName(int columnIndex)
	{
		return this.columnNames[columnIndex];
	}
	
	@Override public Class<?> getColumnClass(int columnIndex)
	{
		
		switch(columnIndex)
		{
			case 0:
				return String.class; // Account Name
				
			case 1:
				return BigDecimal.class; // Total Budgeted Amount
				
			case 2:
				return Periodicity.class; // Periodicity
				
			case 3:
				return String.class; // Line Fund (Name)
				
			default:
				return Object.class;
		}
		
	}
	
	@Override public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		// Account Name is derived, not directly editable here.
		// Periodicity and Line Fund are best edited via ComboBox in JTable cell editor.
		// Total Budgeted Amount is directly editable.
		return columnIndex == 1 || columnIndex == 2 || columnIndex == 3;
	}
	
	@Override public Object getValueAt(int rowIndex, int columnIndex)
	{
		BudgetLine line = this.budgetLines.get(rowIndex);
		
		switch(columnIndex)
		{
			case 0: // Account Name
				if (line.getAccountId() != null && this.chartOfAccounts != null)
				{
					Account account = this.chartOfAccounts.getAccount(line.getAccountId());
					return (account != null) ? account.getName() : "Unknown Account";
				}
				return line.getAccountName() != null ? line.getAccountName() : "N/A";
				
			case 1: // Total Budgeted Amount
				return line.getTotalBudgetedAmount();
				
			case 2: // Periodicity
				return line.getPeriodicity();
				
			case 3: // Line Fund
				if (line.getFundId() != null && !this.availableFunds.isEmpty())
				{
					return this.availableFunds.stream()
						.filter(fund -> line.getFundId().equals(fund.getFundId())) // Assuming Fund
																					// has
																					// getFundId()
						.map(Fund::getName) // Assuming Fund has getName()
						.findFirst()
						.orElse("Unknown Fund ID: " + line.getFundId());
				}
				return "None"; // Or empty if no fund assigned
				
			default:
				return null;
		}
		
	}
	
	@Override public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		BudgetLine line = this.budgetLines.get(rowIndex);
		
		switch(columnIndex)
		{
			case 1: // Total Budgeted Amount
				if (aValue instanceof BigDecimal)
				{
					line.setTotalBudgetedAmount((BigDecimal) aValue);
				}
				else if (aValue instanceof String)
				{
					
					try
					{
						line.setTotalBudgetedAmount(new BigDecimal((String) aValue));
					}
					catch (NumberFormatException e)
					{
						// Handle error or ignore - JTable default editor for BigDecimal might
						// prevent this
						System.err.println("Invalid BigDecimal format: " + aValue);
					}
					
				}
				break;
				
			case 2: // Periodicity
				if (aValue instanceof Periodicity)
				{
					line.setPeriodicity((Periodicity) aValue);
				}
				break;
				
			case 3: // Line Fund (expects fund name, converts to ID)
				if (aValue instanceof String)
				{
					String fundName = (String) aValue;
					
					if ("None".equals(fundName) || fundName.trim().isEmpty())
					{
						line.setFundId(null);
					}
					else
					{
						this.availableFunds.stream()
							.filter(fund -> fundName.equals(fund.getName()))
							.findFirst()
							.ifPresent(fund -> line.setFundId(fund.getFundId()));
					}
					
				}
				break;
		}
		
		fireTableCellUpdated(rowIndex, columnIndex);
	}
	
	public void addRow(BudgetLine line)
	{
		this.budgetLines.add(line);
		fireTableRowsInserted(this.budgetLines.size() - 1, this.budgetLines.size() - 1);
	}
	
	public void removeRow(int rowIndex)
	{
		
		if (rowIndex >= 0 && rowIndex < this.budgetLines.size())
		{
			this.budgetLines.remove(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
		
	}
	
	public BudgetLine getBudgetLineAt(int rowIndex)
	{
		
		if (rowIndex >= 0 && rowIndex < this.budgetLines.size())
		{
			return this.budgetLines.get(rowIndex);
		}
		
		return null;
	}
	
	public List<BudgetLine> getBudgetLines()
	{
		return this.budgetLines;
	}
	
}
