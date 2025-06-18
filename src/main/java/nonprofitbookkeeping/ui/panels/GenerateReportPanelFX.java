
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.reports.ReportContext; // Added import
import nonprofitbookkeeping.ui.actions.GenerateReportsAction;

import java.io.File; // Added import
import java.time.LocalDate; // Added import

/**
 * JavaFX version of {@code GenerateReportPanel}. Lets user pick a report type
 * and runs {@link GenerateReportsAction} via {@link ReportService}.
 */
public class GenerateReportPanelFX extends BorderPane
{
	/**
	 * Constructs a new {@code GenerateReportPanelFX}.
	 * This panel provides a user interface for selecting and generating various types of reports
	 * using the provided {@link ReportService}. It includes a ComboBox for report selection,
	 * a "Generate Report" button, and a TextArea to display output or status messages.
	 * 
	 * @param reportService The {@link ReportService} instance that will be used to generate the reports.
	 *                      It is responsible for the actual report generation logic. Must not be null.
	 */
	public GenerateReportPanelFX(ReportService reportService)
	{
		setPadding(new Insets(10));
		FlowPane pane = new FlowPane(10, 10);
		pane.setPadding(new Insets(10));
		pane.setPrefWrapLength(600);
		
		ComboBox<String> selector = new ComboBox<>();
		selector.getItems().addAll(
        "Income Statement",
        "Balance Sheet",
        "Cash Flow",
        "Trial Balance (Jasper)", // New entry
        "Donor Summary",
        "Fund Activity Report"
    );
		selector.getSelectionModel().selectFirst();
		
		Button generate = new Button("Generate Report");
		TextArea output = new TextArea();
		output.setEditable(false);
		output.setPrefRowCount(10);
		
		generate.setOnAction(e -> {
			String selectedReport = selector.getValue();

			if ("Trial Balance (Jasper)".equals(selectedReport)) {
				output.clear();
				output.appendText("Generating Trial Balance (Jasper) report...\n");

				ReportContext context = new ReportContext();
				context.setReportType("trial_balance_jasper");
				// Placeholder dates as panel doesn't have its own date pickers
				context.setStartDate(LocalDate.now().withDayOfYear(1));
				context.setEndDate(LocalDate.now());

				try {
					File generatedFile = reportService.generateJasperReport(context, "pdf");
					if (generatedFile != null && generatedFile.exists()) {
						output.appendText("\nReport generated successfully: " + generatedFile.getAbsolutePath());
						// Optional: try to open the file
						// try {
						//     java.awt.Desktop.getDesktop().open(generatedFile);
						// } catch (java.io.IOException exDesk) {
						//     output.appendText("\nCould not open file: " + exDesk.getMessage());
						// } catch (UnsupportedOperationException exUnsup) {
                        //     output.appendText("\nDesktop operations not supported on this platform.");
                        // }
					} else {
						output.appendText("\nReport generation failed to produce a file.");
					}
				} catch (Exception ex) {
					output.appendText("\nError generating report: " + ex.getMessage());
					// For debugging, consider: ex.printStackTrace(new java.io.PrintWriter(new javafx.scene.control.TextAreaOutputStream(output)));
                    // Or simply: ex.printStackTrace(); to console
				}

			} else {
				// Keep existing logic for other report types
				output.setText("Generating report: " + selectedReport + "...\n");
				new GenerateReportsAction(reportService).actionPerformed(null); // Assuming this is for JXLS or other reports
				output.appendText("Done. (This is a placeholder for the actual report)");
			}
		});
		
		pane.getChildren().addAll(new Label("Report:"), selector, generate);
		setTop(new TitledPane("Report Selection", pane)
		{
			{
				setCollapsible(false);
			}
			
		});
		setCenter(new TitledPane("Output", new ScrollPane(output))
		{
			{
				setCollapsible(false);
			}
			
		});
	}
	
}
