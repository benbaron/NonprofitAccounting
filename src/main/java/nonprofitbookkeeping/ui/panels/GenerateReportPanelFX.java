
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.reports.ReportContext; // Added import
import nonprofitbookkeeping.reports.ReportTemplates;
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
		
                // Use the statically defined template registry
                java.util.Map<String, ReportTemplates.TemplateInfo> templates = ReportTemplates.templates();
		
		// Display these for the user to select
		ComboBox<String> selector = new ComboBox<>(
													javafx.collections.FXCollections
															.observableArrayList(
																	templates.keySet()));
		
		if (!selector.getItems().isEmpty())
		{
			selector.getSelectionModel().selectFirst();
		}
		
		Button generate = new Button("Generate Report");
		TextArea output = new TextArea();
		output.setEditable(false);
		output.setPrefRowCount(10);
		
		generate.setOnAction(e -> {
                        String selectedReport = selector.getValue();
                        output.clear();
                        output.appendText("Generating " + selectedReport + "...\n");

                        // Create report context
                        ReportContext context = new ReportContext();
                        context.setStartDate(LocalDate.now().withDayOfYear(1));
                        context.setEndDate(LocalDate.now());
                        context.setOutputFormat("pdf");

                        ReportTemplates.TemplateInfo info = templates.get(selectedReport);

                        if (info == null)
                        {
                                output.appendText("Report type not implemented.\n");
                                return;
                        }

                        // Set the report type key derived from the binder class
                        context.setReportType(info.reportTypeKey());
			
			try
			{
				// Generate the report
				File generatedFile = reportService.generateJasperReport(context, "pdf");
				
				if (generatedFile != null && generatedFile.exists())
				{
					output.appendText(
							"\nReport generated successfully: " + 
									generatedFile.getAbsolutePath());
				}
				else
				{
					output.appendText("\nReport generation failed to produce a file.");
				}
				
			}
			catch (Exception ex)
			{
				output.appendText("\nError generating report: " + ex.getMessage());
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
