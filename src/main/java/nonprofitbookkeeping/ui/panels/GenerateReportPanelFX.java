
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
import nonprofitbookkeeping.reports.ReportRowRegistry;
import nonprofitbookkeeping.reports.ReportRowRegistry.RowDefinition;
import nonprofitbookkeeping.ui.actions.GenerateReportsAction;
import nonprofitbookkeeping.ui.panels.ReportRowPanel;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;

/**
 * JavaFX version of {@code GenerateReportPanel}. Lets user pick a report type
 * and runs {@link GenerateReportsAction} via {@link ReportService}.
 */
public class GenerateReportPanelFX extends BorderPane
{
        /**
         * Convenience constructor with no preselected report.
         */
        public GenerateReportPanelFX(ReportService reportService)
        {
                this(reportService, null);
        }

        /**
         * Constructs a new {@code GenerateReportPanelFX}.
         *
         * @param reportService The {@link ReportService} instance that will be used to generate the reports.
         * @param preselectKey  optional report identifier to preselect in the UI.
         */
        public GenerateReportPanelFX(ReportService reportService, String preselectKey)
        {
                setPadding(new Insets(10));

                // Scan templates
                Map<String, String> templates = ReportTemplateScanner.discoverTemplates();
                ReportRowRegistry registry = ReportRowRegistry.getInstance();

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

                final ReportRowPanel[] activePanel = new ReportRowPanel[1];

                selector.setOnAction(e -> {
                        String key = templates.get(selector.getValue());
                        RowDefinition def = registry.lookup(key);
                        if (def != null)
                        {
                                ReportRowPanel panel = def.panelFactory().get();
                                customPanelContainer.getChildren().setAll((javafx.scene.Node) panel);
                                activePanel[0] = panel;
                        }
                        else
                        {
                                activePanel[0] = null;
                                customPanelContainer.getChildren().clear();
                        }

                });

                if (!selector.getItems().isEmpty())
                {
                        if (preselectKey != null)
                        {
                                templates.entrySet().stream()
                                        .filter(e2 -> e2.getValue().equals(preselectKey))
                                        .findFirst()
                                        .ifPresent(e2 -> selector.getSelectionModel().select(e2.getKey()));
                        }
                        else
                        {
                                selector.getSelectionModel().selectFirst();
                        }
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

                        RowDefinition def = registry.lookup(key);
                        if (def != null && activePanel[0] != null)
                        {
                                Object bean = activePanel[0].buildBean();
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
