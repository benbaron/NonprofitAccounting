
package nonprofitbookkeeping.ui.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReportsPanel extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 7644670274679745915L;
	private JComboBox<String> reportTypeCombo;
	private JSpinner startDatePicker;
	private JSpinner endDatePicker;
	private JTable reportTable;
	private DefaultTableModel reportModel;
	private JButton generateButton;
	private JButton exportPdfButton;
	private JButton exportExcelButton;
	
	public ReportsPanel()
	{
		setLayout(new BorderLayout());
		
		// Top controls
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		controlPanel.setBorder(new TitledBorder("Report Options"));
		
		this.reportTypeCombo = new JComboBox<>(new String[]
		{
			"Income Statement", "Balance Sheet", "Trial Balance", "Cash Flow", "General Ledger"
		});
		
		this.startDatePicker = new JSpinner(new SpinnerDateModel());
		this.startDatePicker.setEditor(new JSpinner.DateEditor(this.startDatePicker, "yyyy-MM-dd"));
		this.endDatePicker = new JSpinner(new SpinnerDateModel());
		this.endDatePicker.setEditor(new JSpinner.DateEditor(this.endDatePicker, "yyyy-MM-dd"));
		
		this.generateButton = new JButton("Generate");
		this.exportPdfButton = new JButton("Export to PDF");
		this.exportExcelButton = new JButton("Export to Excel");
		
		controlPanel.add(new JLabel("Report:"));
		controlPanel.add(this.reportTypeCombo);
		controlPanel.add(new JLabel("From:"));
		controlPanel.add(this.startDatePicker);
		controlPanel.add(new JLabel("To:"));
		controlPanel.add(this.endDatePicker);
		controlPanel.add(this.generateButton);
		controlPanel.add(this.exportPdfButton);
		controlPanel.add(this.exportExcelButton);
		
		// Report table
		this.reportModel = new DefaultTableModel(new Object[][] {}, new String[]
		{
			"Account", "Debit", "Credit", "Balance"
		});
		this.reportTable = new JTable(this.reportModel);
		JScrollPane tableScrollPane = new JScrollPane(this.reportTable);
		tableScrollPane.setBorder(new TitledBorder("Report Results"));
		
		// Add components
		add(controlPanel, BorderLayout.NORTH);
		add(tableScrollPane, BorderLayout.CENTER);
		
		// Button logic (stubbed)
		this.generateButton.addActionListener(e -> {
			this.reportModel.setRowCount(0);
			this.reportModel.addRow(new Object[]
			{ "Cash", "1000.00", "0.00", "1000.00" });
			this.reportModel.addRow(new Object[]
			{ "Revenue", "0.00", "500.00", "-500.00" });
		});
		
		this.exportPdfButton.addActionListener(e -> {
			JOptionPane.showMessageDialog(this, "PDF export feature not yet implemented.");
		});
		
		this.exportExcelButton.addActionListener(e -> {
			JOptionPane.showMessageDialog(this, "Excel export feature not yet implemented.");
		});
	}
	
	// Main method for testing.
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Test");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new ReportsPanel());
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
