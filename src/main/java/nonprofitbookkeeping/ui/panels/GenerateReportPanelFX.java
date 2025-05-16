
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.actions.GenerateReportsAction;

/**
 * JavaFX version of {@code GenerateReportPanel}. Lets user pick a report type
 * and runs {@link GenerateReportsAction} via {@link ReportService}.
 */
public class GenerateReportPanelFX extends BorderPane
{
	/**
	 * 
	 * Constructor GenerateReportPanelFX
	 * @param reportService
	 */
	public GenerateReportPanelFX(ReportService reportService)
	{
		setPadding(new Insets(10));
		FlowPane pane = new FlowPane(10, 10);
		pane.setPadding(new Insets(10));
		pane.setPrefWrapLength(600);
		
		ComboBox<String> selector = new ComboBox<>();
		selector.getItems().addAll("Income Statement", "Balance Sheet", "Cash Flow",
			"Donor Summary", "Fund Activity Report");
		selector.getSelectionModel().selectFirst();
		
		Button generate = new Button("Generate Report");
		TextArea output = new TextArea();
		output.setEditable(false);
		output.setPrefRowCount(10);
		
		generate.setOnAction(e -> {
			String rpt = selector.getValue();
			output.setText("Generating report: " + rpt + "...\n");
			new GenerateReportsAction(reportService).actionPerformed(null);
			output.appendText("Done. (This is a placeholder for the actual report)");
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
