package nonprofitbookkeeping.ui.panels;

import java.time.LocalDate;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.report.template.RenderedSemanticReport;
import nonprofitbookkeeping.report.template.WorkbookSemanticReportService;
import nonprofitbookkeeping.ui.UiSpacing;

/** Preview/export panel for semantic JSON workbook-modeled reports. */
public class SemanticReportPreviewPanelFX extends BorderPane
{
	private final WorkbookSemanticReportService reportService =
		new WorkbookSemanticReportService();
	private final Map<String, String> displayNames = this.reportService.displayNames();
	private final ComboBox<String> reportSelector;
	private final DatePicker startDate;
	private final DatePicker endDate;
	private final BorderPane previewHost = new BorderPane();
	private final TextArea exportText = new TextArea();

	public SemanticReportPreviewPanelFX()
	{
		setPadding(PanelChrome.PANEL_PADDING);
		this.reportSelector = new ComboBox<>(
			FXCollections.observableArrayList(this.displayNames.keySet()));
		this.reportSelector.setConverter(new javafx.util.StringConverter<>()
		{
			@Override
			public String toString(String key)
			{
				return key == null ? "" : SemanticReportPreviewPanelFX.this.displayNames.getOrDefault(key, key);
			}

			@Override
			public String fromString(String string)
			{
				return SemanticReportPreviewPanelFX.this.displayNames.entrySet().stream()
					.filter(e -> e.getValue().equals(string))
					.map(Map.Entry::getKey)
					.findFirst()
					.orElse(string);
			}
		});
		if (!this.reportSelector.getItems().isEmpty())
		{
			this.reportSelector.getSelectionModel().selectFirst();
		}
		this.startDate = new DatePicker(LocalDate.now().withDayOfYear(1));
		this.endDate = new DatePicker(LocalDate.now());
		Button preview = new Button("Preview");
		preview.setDefaultButton(true);
		preview.setOnAction(e -> refreshPreview());
		Button export = new Button("Refresh Text/CSV");
		export.setOnAction(e -> refreshExportText());

		HBox controls = new HBox(UiSpacing.SECTION_SPACING,
			new Label("Workbook report:"), this.reportSelector,
			new Label("From:"), this.startDate,
			new Label("To:"), this.endDate,
			preview, export);
		controls.setPadding(new Insets(UiSpacing.SECTION_SPACING));
		setTop(new VBox(6,
			PanelChrome.topSection("Workbook Reports", controls),
			new Separator()));

		this.exportText.setEditable(false);
		this.exportText.setWrapText(false);
		this.exportText.setPrefRowCount(10);
		TitledPane exportPane = new TitledPane("Text/CSV Preview",
			new ScrollPane(this.exportText));
		exportPane.setCollapsible(true);
		exportPane.setExpanded(false);

		VBox center = new VBox(UiSpacing.SECTION_SPACING,
			new TitledPane("Rendered Form", this.previewHost), exportPane);
		VBox.setVgrow(this.previewHost, Priority.ALWAYS);
		setCenter(center);
		refreshPreview();
	}

	private void refreshPreview()
	{
		String templateId = selectedTemplateId();
		if (templateId == null)
		{
			this.previewHost.setCenter(new Label("No workbook report selected."));
			return;
		}
		try
		{
			Node rendered = this.reportService.renderFx(templateId,
				this.startDate.getValue(), this.endDate.getValue());
			this.previewHost.setCenter(rendered);
			refreshExportText();
		}
		catch (RuntimeException ex)
		{
			Label error = new Label("Unable to render workbook report: " + ex.getMessage());
			error.setWrapText(true);
			this.previewHost.setCenter(error);
		}
	}

	private void refreshExportText()
	{
		String templateId = selectedTemplateId();
		if (templateId == null)
		{
			this.exportText.setText("");
			return;
		}
		RenderedSemanticReport report = this.reportService.renderText(templateId,
			this.startDate.getValue(), this.endDate.getValue());
		this.exportText.setText(report.text() + System.lineSeparator()
			+ "--- CSV ---" + System.lineSeparator() + report.csv());
	}

	private String selectedTemplateId()
	{
		return this.reportSelector.getSelectionModel().getSelectedItem();
	}
}
