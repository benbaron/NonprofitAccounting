
package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window; // Added for getScene().getWindow()

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext; // Added import
import nonprofitbookkeeping.reports.jasper.runtime.ReportMetadata;
import nonprofitbookkeeping.reports.jasper.runtime.ReportTemplates;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.jasperreports.engine.design.JRValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JavaFX panel for generating new reports and viewing a list of previously
 * generated reports. It provides UI controls for selecting report type, start
 * date, and end date. Generated reports are displayed in a {@link TableView}
 * with an option to open the report file. The panel uses a
 * {@link ReportService} to handle report generation and listing, and it listens
 * for {@link CurrentCompany} changes to refresh the list of generated reports.
 */
public class SkeletonReportsPanel extends BorderPane
{
	private static final Logger LOGGER =
		LoggerFactory.getLogger(SkeletonReportsPanel.class);
	
	/** ComboBox for selecting the type of report to generate. */
	private ComboBox<String> reportTypeComboBox;
	/** DatePicker for selecting the start date for report generation. */
	private DatePicker startDatePicker;
	/** DatePicker for selecting the end date for report generation. */
	private DatePicker endDatePicker;
	/** ComboBox for selecting the output format. */
	private ComboBox<String> outputFormatComboBox;
	/** Button to trigger the generation of the selected report. */
	private Button generateReportButton;
	/** TableView to display metadata of previously generated reports. */
	private TableView<ReportMetadata> generatedReportsTable;
	/** ObservableList that backs the {@link #generatedReportsTable}, containing {@link ReportMetadata} objects. */
	private ObservableList<ReportMetadata> generatedReportsDataList;
	
	/** Mapping of report display names to their template definitions. */
	private Map<String, ReportTemplates.TemplateInfo> availableTemplates;
	
	/** Service layer for report generation and listing operations. */
	private ReportService reportService;
	/** Listener to react to changes in the {@link CurrentCompany}, triggering a reload of the generated reports list. */
	private CompanyChangeListener companyChangeListener;
	
	/** GridPane containing the report generation controls (type, dates, button). */
	private GridPane controlsGrid;
	/** ScrollPane to ensure report generation controls are accessible if they overflow. */
	private ScrollPane controlsScrollPane;
	
	
	/**
	 * Constructs a new {@code SkeletonReportsPanel}.
	 * Initializes the UI layout, including report generation controls at the top
	 * and a table for displaying previously generated reports in the center.
	 * Sets up table columns, event listeners, and performs an initial load of generated reports.
	 */
	public SkeletonReportsPanel()
	{
		setPadding(new Insets(15));
		this.reportService = new ReportService();
		
		this.generatedReportsDataList = FXCollections.observableArrayList();
		this.generatedReportsTable =
			new TableView<>(this.generatedReportsDataList);
		this.generatedReportsTable
			.setPlaceholder(new Label("No reports found or company not open."));
		
		this.controlsGrid = new GridPane();
		this.controlsGrid.setPadding(new Insets(10));
		this.controlsGrid.setHgap(10);
		this.controlsGrid.setVgap(10);
		
		this.controlsGrid.add(new Label("Report Type:"), 0, 0);
		this.availableTemplates = ReportTemplates.templates();
		
		List<String> sortedReportNames =
			new ArrayList<>(this.availableTemplates.keySet());
		sortedReportNames.sort(String.CASE_INSENSITIVE_ORDER);
		
		this.reportTypeComboBox = new ComboBox<>(
			FXCollections.observableArrayList(sortedReportNames));
		this.reportTypeComboBox.setPromptText("Select Report");
		this.controlsGrid.add(this.reportTypeComboBox, 1, 0);
		
		this.controlsGrid.add(new Label("Start Date:"), 0, 1);
		this.startDatePicker = new DatePicker();
		this.controlsGrid.add(this.startDatePicker, 1, 1);
		
		this.controlsGrid.add(new Label("End Date:"), 0, 2);
		this.endDatePicker = new DatePicker();
		this.controlsGrid.add(this.endDatePicker, 1, 2);
		
		this.controlsGrid.add(new Label("Format:"), 0, 3);
		this.outputFormatComboBox =
			new ComboBox<>(FXCollections.observableArrayList("pdf", "html",
				"xlsx", "text"));
		this.outputFormatComboBox.getSelectionModel().selectFirst();
		this.controlsGrid.add(this.outputFormatComboBox, 1, 3);
		
		this.generateReportButton = new Button("Generate Report");
		this.generateReportButton.setDefaultButton(true);
		HBox buttonBox = new HBox(this.generateReportButton);
		this.controlsGrid.add(buttonBox, 1, 4);
		
		this.controlsScrollPane = new ScrollPane(this.controlsGrid);
		this.controlsScrollPane.setFitToWidth(true);
		this.controlsScrollPane
			.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.controlsScrollPane
			.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		Label panelTitle = new Label("Reports");
		panelTitle.getStyleClass().add("journal-entry-heading");
		this.setTop(new javafx.scene.layout.VBox(5, panelTitle, this.controlsScrollPane));
		
		this.setCenter(this.generatedReportsTable);
		BorderPane.setMargin(this.generatedReportsTable,
			new Insets(10, 0, 0, 0));
		
		setupGeneratedReportsTableColumns();
		setupEventListenersAndRefresh();
		
	}
	
	/**
	 * Sets up the columns for the {@link #generatedReportsTable}.
	 * Defines columns for Report Name, Date Generated, Format, and an Actions column
	 * containing an "Open" button for each report.
	 * The "Open" button attempts to open the report file using the system's default application.
	 * Cell value factories are configured using lambda expressions or {@link PropertyValueFactory}
	 * to bind to properties of the {@link ReportMetadata} class.
	 */
	private void setupGeneratedReportsTableColumns()
	{
		this.generatedReportsTable.getColumns().clear();
		
		TableColumn<ReportMetadata, String> nameCol =
			new TableColumn<>("Report Name");
		nameCol.setCellValueFactory(
			cellData -> new SimpleStringProperty(
				cellData.getValue().getReportName()));
		nameCol.setPrefWidth(250);
		
		TableColumn<ReportMetadata, String> dateGenCol =
			new TableColumn<>("Date Generated");
		dateGenCol.setCellValueFactory(
			cellData -> new SimpleStringProperty(
				cellData.getValue().getCreated()));
		dateGenCol.setPrefWidth(150);
		
		TableColumn<ReportMetadata, String> formatCol =
			new TableColumn<>("Format");
		formatCol.setCellValueFactory(cellData -> {
			String path = cellData.getValue().getFilePath();
			String format = "N/A";
			
			if (path != null && path.contains("."))
			{
				format =
					path.substring(path.lastIndexOf(".") + 1).toUpperCase();
			}
			
			return new SimpleStringProperty(format);
		});
		formatCol.setPrefWidth(80);
		
		TableColumn<ReportMetadata, Void> actionsCol =
			new TableColumn<>("Actions");
		actionsCol.setCellFactory(param -> new TableCell<>()
		{
			private final Button openButton = new Button("Open");
			private final Button dirButton = new Button("Dir");
			private final Button deleteButton = new Button("Delete");
			private final HBox box =
				new HBox(5, this.openButton, this.dirButton, this.deleteButton);
			
			{
				this.openButton.setOnAction(event -> openReport());
				this.dirButton.setOnAction(event -> openDirectory());
				this.deleteButton.setOnAction(event -> deleteReport());
			}
			
			/**
			 * 
			 */
			private void openReport()
			{
				ReportMetadata reportMeta =
					getTableView().getItems().get(getIndex());
				
				if (reportMeta == null || reportMeta.getFilePath() == null)
				{
					AlertBox.showWarning(getScene().getWindow(),
						"Report path is not available.");
					return;
				}
				
				File reportFile = new File(reportMeta.getFilePath());
				
				if (!reportFile.exists())
				{
					AlertBox.showError(getScene().getWindow(),
						"Report file not found: " + reportMeta.getFilePath());
					return;
				}
				
				try
				{
					
					if (Desktop.isDesktopSupported())
					{
						Desktop.getDesktop().open(reportFile);
					}
					else
					{
						AlertBox.showError(getScene().getWindow(),
							"Desktop operations not supported to open file.");
					}
					
				}
				catch (IOException | UnsupportedOperationException ex)
				{
					ex.printStackTrace();
					AlertBox.showError(getScene().getWindow(),
						"Could not open report file: " + ex.getMessage());
				}
				
			}
			

			/**
			 * Open directory.
			 */
			private void openDirectory()
			{
				ReportMetadata reportMeta =
					getTableView().getItems().get(getIndex());
				if (reportMeta == null || reportMeta.getFilePath() == null)
					return;
				
				File reportFile = new File(reportMeta.getFilePath());
				File parent = reportFile.getParentFile();
				
				if (parent != null && parent.exists())
				{
					
					try
					{
						
						if (Desktop.isDesktopSupported())
						{
							Desktop.getDesktop().open(parent);
						}
						
					}
					catch (IOException | UnsupportedOperationException ex)
					{
						ex.printStackTrace();
						AlertBox.showError(getScene().getWindow(),
							"Could not open directory: " + ex.getMessage());
					}
					
				}
				
			}
			
			/**
			 * 
			 */
			private void deleteReport()
			{
				ReportMetadata reportMeta =
					getTableView().getItems().get(getIndex());
				if (reportMeta == null || reportMeta.getFilePath() == null)
					return;
				
				File reportFile = new File(reportMeta.getFilePath());
				
				if (reportFile.exists())
				{
					
					if (reportFile.delete())
					{
						getTableView().getItems().remove(getIndex());
					}
					else
					{
						AlertBox.showError(getScene().getWindow(),
							"Could not delete file: " +
								reportMeta.getFilePath());
					}
					
				}
				
			}
			
			/**
			 * 
			 * @param item
			 * @param empty
			 */
			@Override
			protected void updateItem(Void item, boolean empty)
			{
				super.updateItem(item, empty);
				setGraphic(empty ? null : this.box);
				
			}
			
		});
		actionsCol.setPrefWidth(180);
		
		this.generatedReportsTable.getColumns().addAll(nameCol, dateGenCol,
			formatCol, actionsCol);
		
	}
	
	/**
	 * Loads the list of previously generated reports using the {@link #reportService}
	 * and populates the {@link #generatedReportsTable}.
	 * It clears any existing items in the table before loading new ones.
	 * If an error occurs during loading, an error message is printed to the console,
	 * and a placeholder message is updated in the table.
	 * If no reports are found, an appropriate placeholder message is displayed.
	 */
	private void loadGeneratedReports()
	{
		this.generatedReportsDataList.clear();
		
		if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
		{
			this.generatedReportsTable
				.setPlaceholder(new Label("No company open."));
			return;
		}
		
		try
		{
			List<ReportMetadata> reports = ReportService.listGeneratedReports();
			
			if (reports != null)
			{
				this.generatedReportsDataList.addAll(reports);
			}
			
		}
		catch (Exception e)
		{
			System.err
				.println("Error loading generated reports: " + e.getMessage());
			e.printStackTrace();
			this.generatedReportsTable
				.setPlaceholder(new Label(
					"Could not load generated reports: " + e.getMessage()));
		}
		
		if (this.generatedReportsDataList.isEmpty() &&
			this.generatedReportsTable.getPlaceholder() instanceof Label)
		{
			((Label) this.generatedReportsTable.getPlaceholder())
				.setText("No generated reports found.");
		}
		
	}
	
	/**
	 * Sets up event listeners for UI components and performs an initial data refresh.
	 * This includes:
	 * <ul>
	 *   <li>Registering a {@link CompanyChangeListener} to reload the list of generated reports
	 *       when the current company changes.</li>
	 *   <li>Setting an action handler for the {@link #generateReportButton} to trigger report generation
	 *       based on selected criteria (report type, start/end dates). This process involves
	 *       creating a {@link ReportContext}, calling the {@link ReportService#generateJasperReport(ReportContext, String)},
	 *       and attempting to open the generated file. Alerts are shown for success or errors.</li>
	 *   <li>Performing an initial call to {@link #loadGeneratedReports()} to populate the table.</li>
	 * </ul>
	 */
	private void setupEventListenersAndRefresh()
	{
		this.companyChangeListener = new CompanyChangeListener()
		{
			@Override
			public void companyChange(boolean companyNowOpen)
			{
				loadGeneratedReports();
				
			}
			
		};
		CurrentCompany.CompanyListener
			.addCompanyListener(this.companyChangeListener);
		
		this.generateReportButton.setOnAction(event -> {
			String reportTypeDisplay = this.reportTypeComboBox.getValue();
			Company currentCompany = CurrentCompany.getCompany();
			Window ownerWindow = this.getScene().getWindow();
			
			if (!CurrentCompany.isOpen() || currentCompany == null)
			{
				AlertBox.showError(ownerWindow,
					"No company is currently open.");
				return;
			}
			
			if (reportTypeDisplay == null)
			{
				AlertBox.showError(ownerWindow, "Please select a report type.");
				return;
			}
			
			LocalDate startDate = this.startDatePicker.getValue();
			LocalDate endDate = this.endDatePicker.getValue();
			
			ReportTemplates.TemplateInfo info =
				this.availableTemplates.get(reportTypeDisplay);
			
			if (info == null)
			{
				AlertBox.showError(ownerWindow,
					"Report type '" + reportTypeDisplay +
						"' generation not configured for Jasper system.");
				return;
			}
			
			String reportTypeKey = info.reportTypeKey();
			
			ReportContext ctx = new ReportContext();
			ctx.setReportType(reportTypeKey);
			ctx.setStartDate(startDate);
			ctx.setEndDate(endDate);
			ctx.setFundIds(java.util.Collections.emptyList());
			ctx.setAccountIdsForDetailReport(java.util.Collections.emptyList());
			
			String outputFormat = this.outputFormatComboBox.getValue();
			
			if (outputFormat == null || outputFormat.isEmpty())
			{
				outputFormat = "pdf";
			}
			
			ctx.setOutputFormat(outputFormat);
			
			if (("income_statement_jasper".equals(reportTypeKey) ||
				"cash_flow_statement_jasper".equals(reportTypeKey)) &&
				(startDate == null || endDate == null))
			{
				AlertBox.showError(ownerWindow,
					"Please select both a Start Date and End Date for this report.");
				return;
			}
			
			if (("balance_sheet_jasper".equals(reportTypeKey) ||
				"trial_balance_jasper".equals(reportTypeKey)) &&
				endDate == null)
			{
				AlertBox.showError(ownerWindow,
					"Please select an End Date (As-Of Date) for this report.");
				return;
			}
			
			if (startDate != null && endDate != null &&
				endDate.isBefore(startDate))
			{
				AlertBox.showError(ownerWindow,
					"End Date cannot be before Start Date.");
				return;
			}
			
			try
			{
				File generatedFile;
				
				if ("text".equalsIgnoreCase(outputFormat))
				{
					generatedFile = ReportService.generatePlainTextReport(ctx);
				}
				else
				{
					generatedFile = this.reportService.generateJasperReport(ctx,
						outputFormat);
				}
				
				if (generatedFile != null && generatedFile.exists())
				{
					AlertBox.showInfo(ownerWindow,
						reportTypeDisplay + " generated: " +
							generatedFile.getAbsolutePath());
					
					try
					{
						
						if (Desktop.isDesktopSupported())
						{
							// Check if Desktop API is supported
							Desktop.getDesktop().open(generatedFile);
						}
						else
						{
							AlertBox.showWarning(ownerWindow,
								"Cannot automatically open file. Desktop operations not supported on this platform. File saved at: " +
									generatedFile.getAbsolutePath());
						}
						
					}
					catch (IOException | UnsupportedOperationException ex)
					{
						ex.printStackTrace();
						AlertBox.showError(ownerWindow,
							"Could not open report file: " + ex.getMessage() +
								(ex instanceof UnsupportedOperationException ?
									"\nDesktop operations not supported on this platform." :
									""));
					}
					
				}
				else
				{
					AlertBox.showError(ownerWindow, reportTypeDisplay +
						" could not be generated or found. Check console/logs.");
				}
				
			}
			catch (JRValidationException validationEx)
			{
				LOGGER.error(
					"JasperReports template validation failed for {} (template: {})",
					reportTypeDisplay,
					info != null ? info.jrxmlPath() : "unknown",
					validationEx);
				
				StringBuilder messageBuilder = new StringBuilder();
				messageBuilder.append(reportTypeDisplay)
					.append(
						" could not be generated because its JasperReports template failed validation.");
				
				if (info != null && info.jrxmlPath() != null)
				{
					messageBuilder.append("\nTemplate: ")
						.append(info.jrxmlPath());
				}
				
				String validationMessage = validationEx.getMessage();
				
				if (validationMessage != null && !validationMessage.isBlank())
				{
					messageBuilder.append("\n\nJasperReports details:\n")
						.append(validationMessage.trim());
					
					String diagnosis =
						deriveValidationDiagnosis(validationMessage);
					
					if (diagnosis != null)
					{
						messageBuilder.append("\n\nLikely cause: ")
							.append(diagnosis);
					}
					
				}
				
				messageBuilder.append(
					"\n\nPlease adjust the report layout so that elements fit within their bands, then try again.");
				
				AlertBox.showError(ownerWindow,
					reportTypeDisplay + " template validation failed",
					messageBuilder.toString());
			}
			catch (Exception ex)
			{
				LOGGER.error("Error generating {}", reportTypeDisplay, ex);
				AlertBox.showError(ownerWindow,
					"Error generating " + reportTypeDisplay + ": " +
						ex.getMessage());
			}
			finally
			{
				loadGeneratedReports();
			}
			
		});
		
		loadGeneratedReports();
		
	}
	
	/**
	 * Derive validation diagnosis.
	 *
	 * @param validationMessage the validation message
	 * @return the string
	 */
	private String deriveValidationDiagnosis(String validationMessage)
	{
		
		if (validationMessage == null)
		{
			return null;
		}
		
		String normalized = validationMessage.toLowerCase(Locale.ROOT);
		
		if (normalized.contains("band-height=0"))
		{
			return "The template defines one or more bands with a height of 0. In Jaspersoft Studio, " +
				"set a positive height for sections like Title, Page Header, and Page Footer.";
		}
		
		if (normalized.contains("element bottom reaches outside band area"))
		{
			return "At least one element extends beyond its band height. Increase the band height or " +
				"move the element so it fits within the band.";
		}
		
		return null;
		
	}
	
}
