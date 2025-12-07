
package nonprofitbookkeeping.ui.panels;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportTemplates;
import nonprofitbookkeeping.service.ReportService;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JavaFX version of {@code GenerateReportPanel}. Lets users pick one or more report types
 * and delegates generation to {@link ReportService}.
 */
public class GenerateReportPanelFX extends BorderPane
{
	
	private final ReportService reportService;
	private final Map<String, ReportTemplates.TemplateInfo> templates;
	private final ListView<String> templateList;
	private final ComboBox<String> formatSelector;
	private final TextArea outputArea;
	
	/**
	 * Constructs a new {@code GenerateReportPanelFX}.
	 * This panel provides a user interface for selecting and generating various types of reports
	 * using the provided {@link ReportService}. It now supports generating multiple reports at once
	 * and lets users choose the desired export format.
	 *
	 * @param reportService The {@link ReportService} instance that will be used to generate the reports.
	 *                      It is responsible for the actual report generation logic. Must not be null.
	 */
	public GenerateReportPanelFX(ReportService reportService)
	{
		this.reportService =
			Objects.requireNonNull(reportService, "reportService");
		this.templates = new LinkedHashMap<>(ReportTemplates.templates());
		setPadding(new Insets(10));
		
		this.templateList = new ListView<>(
			FXCollections.observableArrayList(this.templates.keySet()));
		this.templateList.getSelectionModel()
			.setSelectionMode(SelectionMode.MULTIPLE);
		this.templateList.setPrefHeight(
			Math.min(320, Math.max(160, this.templates.size() * 28)));
		
		if (!this.templateList.getItems().isEmpty())
		{
			this.templateList.getSelectionModel().selectFirst();
		}
		
		this.formatSelector = new ComboBox<>(
			FXCollections.observableArrayList("PDF", "HTML", "XLSX"));
		this.formatSelector.getSelectionModel().selectFirst();
		
		Button generate = new Button("Generate Selected");
		generate.setDefaultButton(true);
		generate.setDisable(this.templates.isEmpty());
		generate.setOnAction(e -> generateReports());
		
		VBox selectionBox = new VBox(10);
		selectionBox.setPadding(new Insets(10));
		Label instruction =
			new Label("Choose one or more reports to generate. " +
				"If none are selected, all reports will be generated.");
		instruction.setWrapText(true);
		VBox.setVgrow(this.templateList, Priority.ALWAYS);
		HBox actions = new HBox(10, new Label("Output format:"),
			this.formatSelector, generate);
		selectionBox.getChildren().addAll(instruction, this.templateList,
			actions);
		
		TitledPane selectionPane =
			new TitledPane("Report Selection", selectionBox);
		selectionPane.setCollapsible(false);
		setTop(selectionPane);
		
		this.outputArea = new TextArea();
		this.outputArea.setEditable(false);
		this.outputArea.setWrapText(true);
		this.outputArea.setPrefRowCount(12);
		
		ScrollPane outputScroll = new ScrollPane(this.outputArea);
		outputScroll.setFitToHeight(true);
		outputScroll.setFitToWidth(true);
		
		TitledPane outputPane = new TitledPane("Output", outputScroll);
		outputPane.setCollapsible(false);
		setCenter(outputPane);
		
	}
	
	private void generateReports()
	{
		List<String> selected = new ArrayList<>(
			this.templateList.getSelectionModel().getSelectedItems());
		
		if (selected.isEmpty())
		{
			selected = new ArrayList<>(this.templateList.getItems());
		}
		
		if (selected.isEmpty())
		{
			this.outputArea.appendText("No report templates are available.\n");
			return;
		}
		
		String format = this.formatSelector.getValue().toLowerCase();
		this.outputArea.clear();
		this.outputArea
			.appendText(String.format("Generating %d report%s as %s...%n%n",
				selected.size(), selected.size() == 1 ? "" : "s",
				this.formatSelector.getValue().toUpperCase()));
		
		for (String templateName : selected)
		{
			ReportTemplates.TemplateInfo info =
				this.templates.get(templateName);
			
			if (info == null)
			{
				this.outputArea.appendText(String.format(
					"- %s: template metadata not found.%n", templateName));
				continue;
			}
			
			ReportContext context = createDefaultContext();
			context.setReportType(info.reportTypeKey());
			context.setOutputFormat(format);
			
			try
			{
				File generated =
					this.reportService.generateJasperReport(context, format);
				
				if (generated != null && generated.exists())
				{
					this.outputArea.appendText(String.format("- %s: ✔ %s%n",
						templateName,
						generated.getAbsolutePath()));
				}
				else
				{
					this.outputArea.appendText(
						String.format("- %s: ⚠ No file was produced.%n",
							templateName));
				}
				
			}
			catch (Exception ex)
			{
				String message = ex.getMessage();
				
				if (message == null || message.isBlank())
				{
					message = ex.getClass().getSimpleName();
				}
				
				this.outputArea.appendText(String.format("- %s: ✖ %s%n",
					templateName,
					message));
			}
			
		}
		
		this.outputArea.appendText(
			System.lineSeparator() + "Done." + System.lineSeparator());
		this.outputArea.positionCaret(this.outputArea.getText().length());
		
	}
	
	private ReportContext createDefaultContext()
	{
		ReportContext context = new ReportContext();
		context.setStartDate(LocalDate.now().withDayOfYear(1));
		context.setEndDate(LocalDate.now());
		return context;
		
	}
	
}
