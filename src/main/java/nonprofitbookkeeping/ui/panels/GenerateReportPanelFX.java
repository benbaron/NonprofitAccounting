
package nonprofitbookkeeping.ui.panels;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportTemplateScanner;
import nonprofitbookkeeping.reports.datasource.scareports.RegaliaSalesDtl7Bean;
import nonprofitbookkeeping.reports.datasource.scareports.RegaliaSalesRow;
import nonprofitbookkeeping.ui.actions.GenerateReportsAction;
import nonprofitbookkeeping.ui.panels.scareports.RegaliaSalesPanelFX;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

                // Scan templates
                java.util.Map<String, String> templates =
                        ReportTemplateScanner.discoverTemplates();

                // Report selector and generate button
                ComboBox<String> selector = new ComboBox<>(
                        javafx.collections.FXCollections.observableArrayList(
                                templates.keySet()));
                Button generate = new Button("Generate Report");

                FlowPane selectorPane = new FlowPane(10, 10);
                selectorPane.setPadding(new Insets(10));
                selectorPane.setPrefWrapLength(600);
                selectorPane.getChildren()
                        .addAll(new Label("Report:"), selector, generate);

                StackPane customPanelContainer = new StackPane();
                VBox topBox = new VBox(10, selectorPane, customPanelContainer);

                setTop(new TitledPane("Report Selection", topBox)
                {
                        {
                                setCollapsible(false);
                        }
                });

                TextArea output = new TextArea();
                output.setEditable(false);
                output.setPrefRowCount(10);
                TitledPane outputPane = new TitledPane("Output",
                        new ScrollPane(output))
                {
                        {
                                setCollapsible(false);
                        }
                };
                VBox centerBox = new VBox(10, outputPane);
                setCenter(centerBox);

                final RegaliaSalesPanelFX[] regaliaPanel = new RegaliaSalesPanelFX[1];

                selector.setOnAction(e -> {
                        String key = templates.get(selector.getValue());

                        if (key != null && key.contains("regalia_sales_dtl_7"))
                        {
                                regaliaPanel[0] = new RegaliaSalesPanelFX();
                                customPanelContainer.getChildren().setAll(regaliaPanel[0]);
                        }
                        else
                        {
                                regaliaPanel[0] = null;
                                customPanelContainer.getChildren().clear();
                        }
                });

                if (!selector.getItems().isEmpty())
                {
                        selector.getSelectionModel().selectFirst();
                        selector.getOnAction().handle(null);
                }

                generate.setOnAction(e -> {
                        String selectedReport = selector.getValue();
                        output.clear();
                        output.appendText("Generating " + selectedReport + "...\n");

                        ReportContext context = new ReportContext();
                        context.setStartDate(LocalDate.now().withDayOfYear(1));
                        context.setEndDate(LocalDate.now());
                        context.setOutputFormat("pdf");

                        String key = templates.get(selectedReport);

                        if (key == null)
                        {
                                output.appendText("Report type not implemented.\n");
                                return;
                        }

                        context.setReportType(key);

                        if (regaliaPanel[0] != null &&
                                key.contains("regalia_sales_dtl_7"))
                        {
                                List<RegaliaSalesRow> rows = regaliaPanel[0].getRows();
                                RegaliaSalesDtl7Bean bean = new RegaliaSalesDtl7Bean();
                                bean.setRows(new ArrayList<>(rows));
                                context.setBeans(java.util.Collections
                                                .singletonList(bean));
                        }

                        try
                        {
                                File generatedFile = reportService.generateJasperReport(
                                        context, "pdf");

                                if (generatedFile != null && generatedFile.exists())
                                {
                                        output.appendText(
                                                "\nReport generated successfully: " +
                                                        generatedFile
                                                                .getAbsolutePath());
                                }
                                else
                                {
                                        output.appendText(
                                                "\nReport generation failed to produce a file.");
                                }

                        }
                        catch (Exception ex)
                        {
                                output.appendText(
                                        "\nError generating report: " +
                                                ex.getMessage());
                        }
                });

        }
	
}
