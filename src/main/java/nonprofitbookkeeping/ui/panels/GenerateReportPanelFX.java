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
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.UiSpacing;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JavaFX report generation panel for semantic JSON workbook reports.
 */
public class GenerateReportPanelFX extends BorderPane
{
	private final ReportService reportService;
	private final Map<String, String> displayNames;
	private final ListView<String> templateList;
	private final ComboBox<String> formatSelector;
	private final TextArea outputArea;

	public GenerateReportPanelFX(ReportService reportService)
	{
		this.reportService = Objects.requireNonNull(reportService, "reportService");
		this.displayNames = new nonprofitbookkeeping.report.template.WorkbookSemanticReportService().displayNames();
		setPadding(PanelChrome.PANEL_PADDING);

		this.templateList = new ListView<>(
			FXCollections.observableArrayList(this.displayNames.keySet()));
		this.templateList.getSelectionModel()
			.setSelectionMode(SelectionMode.MULTIPLE);
		this.templateList.setPrefHeight(
			Math.min(320, Math.max(160, this.displayNames.size() * 28)));
		this.templateList.setCellFactory(list -> new javafx.scene.control.ListCell<>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				setText(empty || item == null ? null : displayNames.getOrDefault(item, item));
			}
		});

		if (!this.templateList.getItems().isEmpty())
		{
			this.templateList.getSelectionModel().selectFirst();
		}

		this.formatSelector = new ComboBox<>(
			FXCollections.observableArrayList("Text", "CSV"));
		this.formatSelector.getSelectionModel().selectFirst();

		Button generate = new Button("Generate Selected");
		generate.setDefaultButton(true);
		generate.setDisable(this.displayNames.isEmpty());
		generate.setOnAction(e -> generateReports());

		VBox selectionBox = new VBox(UiSpacing.SECTION_SPACING);
		selectionBox.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		Label instruction =
			new Label("Choose one or more semantic workbook reports to generate. "
				+ "If none are selected, all reports will be generated.");
		instruction.setWrapText(true);
		VBox.setVgrow(this.templateList, Priority.ALWAYS);
		HBox actions = new HBox(UiSpacing.SECTION_SPACING,
			new Label("Output format:"), this.formatSelector, generate);
		selectionBox.getChildren().addAll(instruction, this.templateList, actions);

		TitledPane selectionPane = new TitledPane("Report Selection", selectionBox);
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
			this.outputArea.appendText("No semantic report templates are available.\n");
			return;
		}

		String format = this.formatSelector.getValue().toLowerCase();
		this.outputArea.clear();
		this.outputArea.appendText(String.format(
			"Generating %d semantic report%s as %s...%n%n",
			selected.size(), selected.size() == 1 ? "" : "s",
			this.formatSelector.getValue().toUpperCase()));

		for (String templateId : selected)
		{
			try
			{
				File generated = this.reportService.generateSemanticReport(templateId,
					LocalDate.now().withDayOfYear(1), LocalDate.now(), format);
				this.outputArea.appendText(String.format("- %s: ✔ %s%n",
					this.displayNames.getOrDefault(templateId, templateId),
					generated.getAbsolutePath()));
			}
			catch (Exception ex)
			{
				String message = ex.getMessage();
				if (message == null || message.isBlank())
				{
					message = ex.getClass().getSimpleName();
				}
				this.outputArea.appendText(String.format("- %s: ✖ %s%n",
					this.displayNames.getOrDefault(templateId, templateId), message));
			}
		}

		this.outputArea.appendText(
			System.lineSeparator() + "Done." + System.lineSeparator());
		this.outputArea.positionCaret(this.outputArea.getText().length());
	}
}
