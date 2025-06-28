
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
import nonprofitbookkeeping.reports.ReportContext; // Added import
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.helpers.AlertBox;

import java.util.List;
import java.io.File;
import java.awt.Desktop;
import java.io.IOException;
import java.time.LocalDate;

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
	
	/** ComboBox for selecting the type of report to generate. */
	private ComboBox<String> reportTypeComboBox;
	/** DatePicker for selecting the start date for report generation. */
	private DatePicker startDatePicker;
	/** DatePicker for selecting the end date for report generation. */
	private DatePicker endDatePicker;
	/** Button to trigger the generation of the selected report. */
	private Button generateReportButton;
	/** TableView to display metadata of previously generated reports. */
	private TableView<ReportMetadata> generatedReportsTable;
	/** ObservableList that backs the {@link #generatedReportsTable}, containing {@link ReportMetadata} objects. */
	private ObservableList<ReportMetadata> generatedReportsDataList;
	
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
		this.generatedReportsTable = new TableView<>(this.generatedReportsDataList);
		this.generatedReportsTable
			.setPlaceholder(new Label("No reports found or company not open."));
		
		this.controlsGrid = new GridPane();
		this.controlsGrid.setPadding(new Insets(10));
		this.controlsGrid.setHgap(10);
		this.controlsGrid.setVgap(10);
		
		this.controlsGrid.add(new Label("Report Type:"), 0, 0);
		this.reportTypeComboBox = new ComboBox<>();
		this.reportTypeComboBox.setItems(FXCollections.observableArrayList(
			"Income Statement", "Balance Sheet", "Trial Balance", "Cash Flow Statement"));
		this.reportTypeComboBox.setPromptText("Select Report");
		this.controlsGrid.add(this.reportTypeComboBox, 1, 0);
		
		this.controlsGrid.add(new Label("Start Date:"), 0, 1);
		this.startDatePicker = new DatePicker();
		this.controlsGrid.add(this.startDatePicker, 1, 1);
		
		this.controlsGrid.add(new Label("End Date:"), 0, 2);
		this.endDatePicker = new DatePicker();
		this.controlsGrid.add(this.endDatePicker, 1, 2);
		
		this.generateReportButton = new Button("Generate Report");
		this.generateReportButton.setDefaultButton(true);
		HBox buttonBox = new HBox(this.generateReportButton);
		this.controlsGrid.add(buttonBox, 1, 3);
		
		this.controlsScrollPane = new ScrollPane(this.controlsGrid);
		this.controlsScrollPane.setFitToWidth(true);
		this.controlsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.controlsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		this.setTop(this.controlsScrollPane);
		
		this.setCenter(this.generatedReportsTable);
		BorderPane.setMargin(this.generatedReportsTable, new Insets(10, 0, 0, 0));
		
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
		
		TableColumn<ReportMetadata, String> nameCol = new TableColumn<>("Report Name");
		nameCol.setCellValueFactory(
			cellData -> new SimpleStringProperty(cellData.getValue().getReportName()));
		nameCol.setPrefWidth(250);
		
		TableColumn<ReportMetadata, String> dateGenCol = new TableColumn<>("Date Generated");
		dateGenCol.setCellValueFactory(
			cellData -> new SimpleStringProperty(cellData.getValue().getCreated()));
		dateGenCol.setPrefWidth(150);
		
		TableColumn<ReportMetadata, String> formatCol = new TableColumn<>("Format");
		formatCol.setCellValueFactory(cellData -> {
			String path = cellData.getValue().getFilePath();
			String format = "N/A";
			
			if (path != null && path.contains("."))
			{
				format = path.substring(path.lastIndexOf(".") + 1).toUpperCase();
			}
			
			return new SimpleStringProperty(format);
		});
		formatCol.setPrefWidth(80);
		
		TableColumn<ReportMetadata, Void> actionsCol = new TableColumn<>("Actions");
		actionsCol.setCellFactory(param -> new TableCell<>()
		{
			private final Button openButton = new Button("Open");
			{
				this.openButton.setOnAction(event -> {
					ReportMetadata reportMeta = getTableView().getItems().get(getIndex());
					
					if (reportMeta != null && reportMeta.getFilePath() != null)
					{
						
						try
						{
							File reportFile = new File(reportMeta.getFilePath());
							
							if (reportFile.exists())
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
							else
							{
								AlertBox.showError(getScene().getWindow(),
									"Report file not found: " + reportMeta.getFilePath());
							}
							
						}
						catch (IOException e)
						{
							e.printStackTrace();
							AlertBox.showError(getScene().getWindow(),
								"Could not open report file: " + e.getMessage());
						}
						catch (UnsupportedOperationException e)
						{
							e.printStackTrace();
							AlertBox.showError(getScene().getWindow(),
								"Desktop operations not supported on this platform (e.g. headless server).");
						}
						
					}
					else
					{
						AlertBox.showWarning(getScene().getWindow(),
							"Report path is not available.");
					}
					
				});
			}
			
			@Override protected void updateItem(Void item, boolean empty)
			{
				super.updateItem(item, empty);
				setGraphic(empty ? null : this.openButton);
			}
			
		});
		actionsCol.setPrefWidth(100);
		
		this.generatedReportsTable.getColumns()
			.addAll(nameCol,
				dateGenCol,
				formatCol,
				actionsCol);
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
			List<ReportMetadata> reports = this.reportService.listGeneratedReports();
			
			if (reports != null)
			{
				this.generatedReportsDataList.addAll(reports);
			}
			
		}
		catch (Exception e)
		{
			System.err.println("Error loading generated reports: " + e.getMessage());
			e.printStackTrace();
			this.generatedReportsTable
				.setPlaceholder(new Label("Could not load generated reports: " + e.getMessage()));
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
			@Override public void companyChange(boolean companyNowOpen)
			{
				loadGeneratedReports();
			}
			
		};
		CurrentCompany.CompanyListener.addCompanyListener(this.companyChangeListener);
		
		this.generateReportButton.setOnAction(event -> {
			String reportTypeDisplay = this.reportTypeComboBox.getValue();
			Company currentCompany = CurrentCompany.getCompany();
			Window ownerWindow = this.getScene().getWindow();
			
			if (!CurrentCompany.isOpen() || currentCompany == null)
			{
				AlertBox.showError(ownerWindow, "No company is currently open.");
				return;
			}
			
			if (reportTypeDisplay == null)
			{
				AlertBox.showError(ownerWindow, "Please select a report type.");
				return;
			}
			
			LocalDate startDate = this.startDatePicker.getValue();
			LocalDate endDate = this.endDatePicker.getValue();
			
			String reportTypeKey;
			boolean isJasperReport = true;
			
			switch(reportTypeDisplay)
			{
				case "Income Statement":
					reportTypeKey = "income_statement_jasper";
					break;
				
				case "Balance Sheet":
					reportTypeKey = "balance_sheet_jasper";
					AlertBox.showInfo(ownerWindow,
						"Balance Sheet via Jasper is chosen, but ensure its generator is fully implemented in ReportService.");
					break;
				
				case "Trial Balance":
					reportTypeKey = "trial_balance_jasper";
					AlertBox.showInfo(ownerWindow,
						"Trial Balance via Jasper is chosen, but ensure its generator is fully implemented in ReportService.");
					break;
				
				case "Cash Flow Statement":
					reportTypeKey = "cash_flow_statement_jasper";
					break;
				
				default:
					AlertBox.showError(ownerWindow, "Report type '" +
						reportTypeDisplay +
						"' generation not configured for Jasper system.");
					return;
			}
			
                        ReportContext ctx = new ReportContext();
                        ctx.setReportType(reportTypeKey);
                        ctx.setStartDate(startDate);
                        ctx.setEndDate(endDate);
                        ctx.setFundIds(java.util.Collections.emptyList());
                        ctx.setSelectedBudget(null);
                        ctx.setAccountIdsForDetailReport(java.util.Collections.emptyList());
			
			String outputFormat = "pdf";
			
			if (isJasperReport)
			{
				
				if (("income_statement_jasper".equals(reportTypeKey) ||
					"cash_flow_statement_jasper".equals(reportTypeKey)) &&
					(startDate == null || endDate == null))
				{
					AlertBox.showError(ownerWindow,
						"Please select both a Start Date and End Date for this report.");
					return;
				}
				
				if (("balance_sheet_jasper".equals(reportTypeKey) ||
					"trial_balance_jasper".equals(reportTypeKey)) && endDate == null)
				{
					AlertBox.showError(ownerWindow,
						"Please select an End Date (As-Of Date) for this report.");
					return;
				}
				
				if (startDate != null && endDate != null && endDate.isBefore(startDate))
				{
					AlertBox.showError(ownerWindow,
						"End Date cannot be before Start Date.");
					return;
				}
				
				try
				{
					File generatedFile = this.reportService.generateJasperReport(ctx, outputFormat);
					
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
								"Could not open report file: " + 
							ex.getMessage() +
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
				catch (Exception ex)
				{
					ex.printStackTrace();
					AlertBox.showError(ownerWindow,
						"Error generating " + reportTypeDisplay + ": " + ex.getMessage());
				}
				finally
				{
					loadGeneratedReports();
				}
				
			}
			else
			{
				// This block would handle non-Jasper reports if any were configured
				AlertBox.showInfo(ownerWindow, 
					"Generation for non-Jasper report type '" +
					reportTypeDisplay + 
					"' is not handled by this path.");
			}
			
		});
		
		loadGeneratedReports();
	}
	
}
