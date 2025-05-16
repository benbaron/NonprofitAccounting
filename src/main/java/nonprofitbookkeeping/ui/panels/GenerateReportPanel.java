
package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.actions.GenerateReportsAction;

import javax.swing.*;
import java.awt.*;

public class GenerateReportPanel extends JPanel
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 3149616785584949291L;
	
	public GenerateReportPanel(ReportService reportService)
	{
		setLayout(new BorderLayout());
		
		// Panel for report selection
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(BorderFactory.createTitledBorder("Select Report"));
		
		// JComboBox for selecting report type
		JComboBox<String> reportSelector = new JComboBox<>(new String[]
		{
			"Income Statement", "Balance Sheet", "Cash Flow", "Donor Summary",
			"Fund Activity Report"
		});
		
		// Button to generate the report
		JButton generateButton = new JButton("Generate Report");
		
		// Output area to display result (could be an HTML or PDF file path)
		JTextArea outputArea = new JTextArea(10, 50);
		outputArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(outputArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));
		
		// Button ActionListener to trigger report generation
		generateButton.addActionListener(e -> {
			String report = (String) reportSelector.getSelectedItem();
			outputArea.setText("Generating report: " + report + "...\n");
			
			// Create the GenerateReportsAction and execute it
			GenerateReportsAction action = new GenerateReportsAction(reportService);
			action.actionPerformed(e); // This will prompt for user input and generate the report
			
			outputArea.append("Done. (This is a placeholder for the actual report)");
		});
		
		// Add components to the panel
		panel.add(new JLabel("Report:"));
		panel.add(reportSelector);
		panel.add(generateButton);
		
		add(panel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	// Main method for testing.
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			// Example for creating the panel with a dummy ReportService (replace with
			// actual service)
			ReportService reportService = new ReportService(); // Your ReportService should be
																// implemented
			JFrame frame = new JFrame("Generate Report Panel Test");
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.getContentPane().add(new GenerateReportPanel(reportService));
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}
	
}
