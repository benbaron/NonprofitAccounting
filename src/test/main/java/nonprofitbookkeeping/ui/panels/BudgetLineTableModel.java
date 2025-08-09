
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Fund;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * A Swing {@link javax.swing.table.AbstractTableModel} implementation for displaying
 * and managing a list of {@link BudgetLine} objects.
 * This model defines columns for Account Name, Total Budgeted Amount, Periodicity, and Line Fund.
 * It uses a {@link ChartOfAccounts} instance to resolve account names from account IDs
 * and a list of available {@link Fund}s to resolve fund names for display.
 */
public class BudgetLineTableModel extends AbstractTableModel
{
	/** The list of {@link BudgetLine} objects this table model represents. */
	private final List<BudgetLine> budgetLines;
	/** The {@link ChartOfAccounts} used to look up account names based on IDs stored in budget lines. */
	private final ChartOfAccounts chartOfAccounts;
	/** A list of all available {@link Fund}s, used to look up fund names for display. */
	private final List<Fund> availableFunds;
	
	/** Column names for the table. */
	private final String[] columnNames =
	{ "Account Name", "Total Budgeted Amount", "Periodicity", "Line Fund" };
	
	/**
     * Constructs a new {@code BudgetLineTableModel}.
     *
     * @param budgetLines The list of {@link BudgetLine}s to be managed by this model.
     *                    This list will be directly used (not copied). Must not be null.
     * @param chartOfAccounts The {@link ChartOfAccounts} instance for resolving account details. Must not be null.
     * @param availableFunds A list of all available {@link Fund}s for resolving fund details.
     *                       If null, an empty list will be used.
     * @throws NullPointerException if {@code budgetLines} or {@code chartOfAccounts} is null.
     */
	public BudgetLineTableModel(List<BudgetLine> budgetLines, ChartOfAccounts chartOfAccounts,
		List<Fund> availableFunds)
	{
		this.budgetLines = Objects.requireNonNull(budgetLines, "budgetLines cannot be null");
		this.chartOfAccounts = Objects.requireNonNull(chartOfAccounts, "chartOfAccounts cannot be null");
		this.availableFunds = availableFunds != null ? availableFunds : List.of(); // Ensure non-null
	}
	
	/**
     * {@inheritDoc}
     * @return The number of budget lines managed by this model.
     */
	@Override public int getRowCount()
	{
		return this.budgetLines.size();
	}
	
	/**
     * {@inheritDoc}
     * @return The number of columns, which is fixed by {@link #columnNames}.
     */
	@Override public int getColumnCount()
	{
		return this.columnNames.length;
	}
	
	/**
     * {@inheritDoc}
     * @param columnIndex The index of the column.
     * @return The name of the column at {@code columnIndex}.
     */
	@Override public String getColumnName(int columnIndex)
	{
		return this.columnNames[columnIndex];
	}
	
	/**
     * {@inheritDoc}
     * Returns the class type for each column:
     * <ul>
     *   <li>Column 0 (Account Name): {@code String.class}</li>
     *   <li>Column 1 (Total Budgeted Amount): {@code BigDecimal.class}</li>
     *   <li>Column 2 (Periodicity): {@code Periodicity.class}</li>
     *   <li>Column 3 (Line Fund): {@code String.class} (displays fund name)</li>
     *   <li>Default: {@code Object.class}</li>
     * </ul>
     * @param columnIndex The index of the column.
     * @return The {@link Class} of the data in the specified column.
     */
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
	
	/**
     * {@inheritDoc}
     * Determines if a cell is editable.
     * <ul>
     *   <li>Account Name (column 0) is not directly editable (derived from Account ID).</li>
     *   <li>Total Budgeted Amount (column 1) is editable.</li>
     *   <li>Periodicity (column 2) is editable (typically via a ComboBox editor in the JTable).</li>
     *   <li>Line Fund (column 3) is editable (typically via a ComboBox editor in the JTable).</li>
     * </ul>
     * @param rowIndex The row being queried.
     * @param columnIndex The column being queried.
     * @return True if the cell at the specified row and column is editable, false otherwise.
     */
	@Override public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		// Account Name is derived, not directly editable here.
		// Periodicity and Line Fund are best edited via ComboBox in JTable cell editor.
		// Total Budgeted Amount is directly editable.
		return columnIndex == 1 || columnIndex == 2 || columnIndex == 3;
	}
	
	/**
     * {@inheritDoc}
     * <p>Retrieves the value for a cell in the table.
     * <ul>
     *   <li>Column 0 (Account Name): Resolves the account name from the {@code budgetLine}'s account ID
     *       using the {@code chartOfAccounts}. Shows "Unknown Account" or "N/A" if not found.</li>
     *   <li>Column 1 (Total Budgeted Amount): Returns {@link BudgetLine#getTotalBudgetedAmount()}.</li>
     *   <li>Column 2 (Periodicity): Returns {@link BudgetLine#getPeriodicity()}.</li>
     *   <li>Column 3 (Line Fund): Resolves the fund name from the {@code budgetLine}'s fund ID
     *       using {@code availableFunds}. Shows "Unknown Fund ID" or "None".</li>
     * </ul>
     * </p>
     * @param rowIndex The row whose value is to be queried.
     * @param columnIndex The column whose value is to be queried.
     * @return The value at the specified cell.
     */
	@Override public Object getValueAt(int rowIndex, int columnIndex)
	{
		BudgetLine line = this.budgetLines.get(rowIndex);
		
		switch(columnIndex)
		{
			case 0: // Account Name
				if (line.getAccountId() != null && this.chartOfAccounts != null)
				{
					Account account = this.chartOfAccounts.getAccount(line.getAccountId());
					return (account != null && account.getName() != null) ? account.getName() : "Unknown Account ID: " + line.getAccountId();
				}
				return line.getAccountName() != null ? line.getAccountName() : "N/A"; // Fallback to stored name if any
				
			case 1: // Total Budgeted Amount
				return line.getTotalBudgetedAmount();
				
			case 2: // Periodicity
				return line.getPeriodicity();
				
			case 3: // Line Fund
				if (line.getFundId() != null && !this.availableFunds.isEmpty())
				{
					return this.availableFunds.stream()
						.filter(fund -> fund != null && line.getFundId().equals(fund.getFundId()))
						.map(Fund::getName)
						.findFirst()
						.orElse("Unknown Fund ID: " + line.getFundId());
				}
				return "None"; // Or empty string if no fund assigned
				
			default:
				return null;
		}
		
	}
	
	/**
     * {@inheritDoc}
     * <p>Sets the value for a cell in the table. This method supports editing for:
     * <ul>
     *   <li>Column 1 (Total Budgeted Amount): Accepts {@link BigDecimal} or a String parseable to BigDecimal.</li>
     *   <li>Column 2 (Periodicity): Accepts a {@link Periodicity} enum value.</li>
     *   <li>Column 3 (Line Fund): Accepts a String representing the fund name. It attempts to find the
     *       corresponding fund ID from {@code availableFunds}. "None" or empty string sets fund ID to null.</li>
     * </ul>
     * After setting a value, it fires a {@code fireTableCellUpdated} event.
     * </p>
     * @param aValue The new value for the cell.
     * @param rowIndex The row of the cell to be changed.
     * @param columnIndex The column of the cell to be changed.
     */
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
						line.setTotalBudgetedAmount(new BigDecimal(((String) aValue).trim()));
					}
					catch (NumberFormatException e)
					{
						// Error handling for invalid input string for BigDecimal
						System.err.println("Invalid BigDecimal format for input: " + aValue);
						// Optionally, show an error dialog to the user
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
					String fundName = ((String) aValue).trim();
					
					if ("None".equalsIgnoreCase(fundName) || fundName.isEmpty())
					{
						line.setFundId(null);
					}
					else
					{
						// Find fund by name and set its ID
						this.availableFunds.stream()
							.filter(fund -> fund != null && fundName.equals(fund.getName()))
							.findFirst()
							.ifPresentOrElse(
                                fund -> line.setFundId(fund.getFundId()),
                                () -> System.err.println("Fund not found for name: " + fundName) // Fund name from editor not in availableFunds
                            );
					}
					
				}
				break;
			default:
				break;
		}
		
		fireTableCellUpdated(rowIndex, columnIndex); // Notify listeners of cell update
	}
	
	/**
     * Adds a new {@link BudgetLine} to the table model.
     * Notifies listeners that a row has been inserted.
     *
     * @param line The {@link BudgetLine} to add.
     */
	public void addRow(BudgetLine line)
	{
		this.budgetLines.add(line);
		fireTableRowsInserted(this.budgetLines.size() - 1, this.budgetLines.size() - 1);
	}
	
	/**
     * Removes the {@link BudgetLine} at the specified row index from the table model.
     * Notifies listeners that a row has been deleted.
     *
     * @param rowIndex The zero-based index of the row to remove.
     */
	public void removeRow(int rowIndex)
	{
		
		if (rowIndex >= 0 && rowIndex < this.budgetLines.size())
		{
			this.budgetLines.remove(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
		
	}
	
	/**
     * Retrieves the {@link BudgetLine} object at the specified row index.
     *
     * @param rowIndex The zero-based index of the row.
     * @return The {@link BudgetLine} at the specified row, or null if the index is out of bounds.
     */
	public BudgetLine getBudgetLineAt(int rowIndex)
	{
		
		if (rowIndex >= 0 && rowIndex < this.budgetLines.size())
		{
			return this.budgetLines.get(rowIndex);
		}
		
		return null; // Or throw IndexOutOfBoundsException
	}
	
	/**
     * Gets the underlying list of {@link BudgetLine} objects managed by this model.
     * Note: Modifying this list directly will not fire table model events.
     * Use {@link #addRow(BudgetLine)} and {@link #removeRow(int)} for modifications
     * that update the table view.
     *
     * @return The list of budget lines.
     */
	public List<BudgetLine> getBudgetLines()
	{
		return this.budgetLines; // Exposes internal list; consider returning a copy if immutability is desired from this getter
	}
	
}
