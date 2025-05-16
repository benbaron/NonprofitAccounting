
package nonprofitbookkeeping.ui.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JournalPanel extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6603652614905520173L;
	private JTable entryTable;
	private DefaultTableModel entryModel;
	private JTable journalTable;
	private DefaultTableModel journalModel;
	private JSpinner datePicker;
	private JTextField descriptionField;
	private JTextField filterField;
	private JLabel balanceStatus;
	
	/**
	 * 
	 * Constructor JournalPanel
	 */
	public JournalPanel()
	{
		setLayout(new BorderLayout());
		
		// Entry Form Panel
		JPanel formPanel = new JPanel(new BorderLayout());
		formPanel.setBorder(new TitledBorder("New Journal Entry"));
		
		JPanel topForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
		topForm.add(new JLabel("Date:"));
		this.datePicker = new JSpinner(new SpinnerDateModel());
		this.datePicker.setEditor(new JSpinner.DateEditor(this.datePicker, "yyyy-MM-dd"));
		topForm.add(this.datePicker);
		
		topForm.add(new JLabel("Description:"));
		this.descriptionField = new JTextField(20);
		topForm.add(this.descriptionField);
		
		formPanel.add(topForm, BorderLayout.NORTH);
		
		// Debit/Credit Table
		String[] entryCols =
		{ "Account", "Amount", "Memo", "Type" };
		this.entryModel = new DefaultTableModel(null, entryCols)
		{
			/**
			 * serialVersionUID : long
			 */
			private static final long serialVersionUID = 7953573165658738637L;

			@Override public boolean isCellEditable(int row, int column)
			{
				return true;
			}
			
		};
		this.entryTable = new JTable(this.entryModel)
		{
			/**
			 * serialVersionUID : long
			 */
			private static final long serialVersionUID = 1076486771176498189L;

			@Override public TableCellEditor getCellEditor(int row, int column)
			{
				
				switch(column)
				{
					case 0:
						return new DefaultCellEditor(new JComboBox<>(new String[]
						{
							"Cash", "Bank", "Grants", "Donations", "Utilities", "Programs"
						}));
						
					case 3:
						return new DefaultCellEditor(new JComboBox<>(new String[]
						{ "Debit", "Credit" }));
						
					default:
						return super.getCellEditor(row, column);
				}
				
			}
			
		};
		JScrollPane entryScrollPane = new JScrollPane(this.entryTable);
		formPanel.add(entryScrollPane, BorderLayout.CENTER);
		
		// Entry control buttons
		JPanel entryButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton addRow = new JButton("Add Row");
		JButton removeRow = new JButton("Remove Row");
		JButton saveEntry = new JButton("Save Entry");
		this.balanceStatus = new JLabel(" ");
		this.balanceStatus.setForeground(Color.RED);
		
		addRow.addActionListener(e -> this.entryModel.addRow(new Object[]
		{ "Cash", "", "", "Debit" }));
		removeRow.addActionListener(e -> {
			int selected = this.entryTable.getSelectedRow();
			if (selected != -1)
				this.entryModel.removeRow(selected);
		});
		saveEntry.addActionListener(this::handleSaveEntry);
		
		entryButtons.add(addRow);
		entryButtons.add(removeRow);
		entryButtons.add(saveEntry);
		entryButtons.add(this.balanceStatus);
		formPanel.add(entryButtons, BorderLayout.SOUTH);
		
		// Filter/Search bar
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchPanel.setBorder(new TitledBorder("Search Entries"));
		searchPanel.add(new JLabel("Filter:"));
		this.filterField = new JTextField(20);
		searchPanel.add(this.filterField);
		JButton filterBtn = new JButton("Apply");
		searchPanel.add(filterBtn);
		
		// Journal Table
		String[] journalCols =
		{ "Date", "Description", "Amount" };
		this.journalModel = new DefaultTableModel(journalCols, 0);
		this.journalTable = new JTable(this.journalModel);
		JScrollPane journalScrollPane = new JScrollPane(this.journalTable);
		journalScrollPane.setBorder(new TitledBorder("Saved Journal Entries"));
		
		// Assemble full layout
		add(formPanel, BorderLayout.NORTH);
		add(searchPanel, BorderLayout.CENTER);
		add(journalScrollPane, BorderLayout.SOUTH);
	}
	
	/**
	 * 
	 * @param e
	 */
	private void handleSaveEntry(ActionEvent e)
	{
		double debitTotal = 0;
		double creditTotal = 0;
		
		for (int i = 0; i < this.entryModel.getRowCount(); i++)
		{
			Object amountObj = this.entryModel.getValueAt(i, 1);
			Object typeObj = this.entryModel.getValueAt(i, 3);
			if (amountObj == null || typeObj == null)
				continue;
			
			String amountStr = amountObj.toString();
			String type = typeObj.toString();
			
			try
			{
				double amount = Double.parseDouble(amountStr);
				if ("Debit".equalsIgnoreCase(type))
					debitTotal += amount;
				else if ("Credit".equalsIgnoreCase(type))
					creditTotal += amount;
			}
			catch (NumberFormatException ex)
			{
				this.balanceStatus.setText("Invalid amount at row " + (i + 1));
				return;
			}
			
		}
		
		if (Math.abs(debitTotal - creditTotal) > 0.01)
		{
			this.balanceStatus.setText("Entries do not balance.");
		}
		else
		{
			Date selectedDate = (Date) this.datePicker.getValue();
			String date = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
			String desc = this.descriptionField.getText();
			this.journalModel.addRow(new Object[]
			{ date, desc, String.format("$%.2f", debitTotal) });
			this.entryModel.setRowCount(0); // Clear entry table
			this.balanceStatus.setText("Entry saved.");
		}
		
	}
    // Main method for testing.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add(new JournalPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
	
}
