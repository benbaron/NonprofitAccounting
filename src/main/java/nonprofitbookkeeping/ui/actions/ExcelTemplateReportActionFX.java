package nonprofitbookkeeping.ui.actions;

import nonprofitbookkeeping.reports.excel.ExcelWorkbookPageReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.preferences.PreferencesManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Action for generating a single-sheet Excel template report.
 */
public class ExcelTemplateReportActionFX implements EventHandler<ActionEvent>
{
	private final Stage owner;
	private final ExcelWorkbookPageReportService reportService =
		new ExcelWorkbookPageReportService();
	private final TextField templateField = new TextField();
	private final TextField outputField = new TextField();
	private final TextField sheetField = new TextField();
	private final TextField fieldMapField = new TextField();
	private final TextField beanClassField = new TextField();
	private final TextArea sqlArea = new TextArea();

	public ExcelTemplateReportActionFX(Stage owner)
	{
		this.owner = owner;
	}

	@Override
	public void handle(ActionEvent event)
	{
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Excel Template Report");
		dialog.setHeaderText("Populate a worksheet using a template map and SQL.");
		if (this.owner != null)
		{
			dialog.initOwner(this.owner);
		}

		ButtonType runButton = new ButtonType("Run",
			ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(runButton,
			ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10));

		this.sqlArea.setPrefRowCount(8);

		grid.add(new Label("Template file:"), 0, 0);
		grid.add(buildFilePickerRow(this.templateField, "Choose...",
			this::chooseTemplateFile), 1, 0);
		grid.add(new Label("Output file:"), 0, 1);
		grid.add(buildFilePickerRow(this.outputField, "Save As...",
			this::chooseOutputFile), 1, 1);
		grid.add(new Label("Sheet name:"), 0, 2);
		grid.add(this.sheetField, 1, 2);
		grid.add(new Label("Field map resource:"), 0, 3);
		grid.add(this.fieldMapField, 1, 3);
		grid.add(new Label("Bean class:"), 0, 4);
		grid.add(this.beanClassField, 1, 4);
		grid.add(new Label("SQL:"), 0, 5);
		grid.add(this.sqlArea, 1, 5);

		dialog.getDialogPane().setContent(grid);

		dialog.showAndWait().ifPresent(result -> {
			if (result != runButton)
			{
				return;
			}

			String templatePath = this.templateField.getText();
			String outputPath = this.outputField.getText();
			String sheetName = this.sheetField.getText();
			String fieldMapResource = this.fieldMapField.getText();
			String beanClassName = this.beanClassField.getText();
			String sql = this.sqlArea.getText();

			if (isBlank(templatePath) || isBlank(outputPath)
				|| isBlank(sheetName) || isBlank(fieldMapResource)
				|| isBlank(beanClassName) || isBlank(sql))
			{
				AlertBox.showError(this.owner,
					"All fields are required to run the report.");
				return;
			}

			File templateFile = new File(templatePath);
			File outputFile = new File(outputPath);
			if (!validateInputs(templateFile, outputFile))
			{
				return;
			}

			Class<?> beanClass;
			try
			{
				beanClass = Class.forName(beanClassName.trim());
			}
			catch (ClassNotFoundException ex)
			{
				AlertBox.showError(this.owner,
					"Bean class not found: " + beanClassName);
				return;
			}

			try
			{
				reportService.populateWorkbookPage(
					templateFile,
					outputFile,
					sheetName.trim(),
					fieldMapResource.trim(),
					beanClass,
					sql.trim()
				);
				AlertBox.showInfo(this.owner,
					"Report generated: " + outputFile.getAbsolutePath());
			}
			catch (IOException | RuntimeException ex)
			{
				AlertBox.showError(this.owner,
					"Failed to generate report: " + ex.getMessage());
			}
		});
	}

	private HBox buildFilePickerRow(TextField target, String buttonText,
		EventHandler<ActionEvent> handler)
	{
		Button button = new Button(buttonText);
		button.setOnAction(handler);
		HBox box = new HBox(8, target, button);
		target.setPrefWidth(360);
		return box;
	}

	private void chooseTemplateFile(ActionEvent event)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Template Workbook");
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("Excel files (*.xlsx, *.xlsm)",
				"*.xlsx", "*.xlsm"));
		setInitialDirectory(chooser, PreferencesManager.getLastDirectory());
		File selected = chooser.showOpenDialog(this.owner);
		if (selected != null)
		{
			this.templateField.setText(selected.getAbsolutePath());
			rememberLastDirectory(selected);
		}
	}

	private void chooseOutputFile(ActionEvent event)
	{
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Save Output Workbook");
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter("Excel files (*.xlsx)",
				"*.xlsx"));
		setInitialDirectory(chooser, PreferencesManager.getLastWriteDirectory());
		File selected = chooser.showSaveDialog(this.owner);
		if (selected != null)
		{
			this.outputField.setText(selected.getAbsolutePath());
			rememberLastWriteDirectory(selected);
		}
	}

	private boolean isBlank(String value)
	{
		return value == null || value.trim().isEmpty();
	}

	private boolean validateInputs(File templateFile, File outputFile)
	{
		if (!templateFile.exists() || !templateFile.isFile())
		{
			AlertBox.showError(this.owner,
				"Template file does not exist: " + templateFile.getAbsolutePath());
			return false;
		}

		File outputDir = outputFile.getParentFile();
		if (outputDir != null && !outputDir.exists())
		{
			AlertBox.showError(this.owner,
				"Output directory does not exist: " + outputDir.getAbsolutePath());
			return false;
		}

		return true;
	}

	private void setInitialDirectory(FileChooser chooser, String pathValue)
	{
		if (pathValue == null || pathValue.isBlank())
		{
			return;
		}
		try
		{
			Path path = Path.of(pathValue);
			File dir = path.toFile();
			if (dir.isDirectory())
			{
				chooser.setInitialDirectory(dir);
			}
		}
		catch (InvalidPathException ex)
		{
			// ignore invalid stored preference
		}
	}

	private void rememberLastDirectory(File file)
	{
		if (file == null)
		{
			return;
		}
		File dir = file.getParentFile();
		if (dir != null)
		{
			PreferencesManager.setLastDirectory(dir.getAbsolutePath());
		}
	}

	private void rememberLastWriteDirectory(File file)
	{
		if (file == null)
		{
			return;
		}
		File dir = file.getParentFile();
		if (dir != null)
		{
			PreferencesManager.setLastWriteDirectory(dir.getAbsolutePath());
		}
	}
}
