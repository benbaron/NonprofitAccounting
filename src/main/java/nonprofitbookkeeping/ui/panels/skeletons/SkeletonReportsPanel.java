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
// PropertyValueFactory is not strictly needed if using lambdas for all columns
// import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.service.ReportService; // Assuming this exists and is correct
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.awt.Desktop; // For opening files
import java.io.IOException;
import java.time.LocalDate; // For DatePicker values

public class SkeletonReportsPanel extends BorderPane {

    private ComboBox<String> reportTypeComboBox;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Button generateReportButton;
    private TableView<ReportMetadata> generatedReportsTable;
    private ObservableList<ReportMetadata> generatedReportsDataList;

    private ReportService reportService;
    private CompanyChangeListener companyChangeListener;

    // UI elements for layout
    private GridPane controlsGrid;
    private ScrollPane controlsScrollPane;


    public SkeletonReportsPanel() {
        setPadding(new Insets(15)); // Overall padding
        reportService = new ReportService(); // Initialize service

        // Initialize collections and table
        generatedReportsDataList = FXCollections.observableArrayList();
        generatedReportsTable = new TableView<>(generatedReportsDataList);
        generatedReportsTable.setPlaceholder(new Label("No reports found or company not open."));

        // Report Generation Controls (Top)
        controlsGrid = new GridPane();
        controlsGrid.setPadding(new Insets(10));
        controlsGrid.setHgap(10);
        controlsGrid.setVgap(10);

        controlsGrid.add(new Label("Report Type:"), 0, 0);
        reportTypeComboBox = new ComboBox<>();
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Income Statement", "Balance Sheet", "Trial Balance", "Cash Flow Statement"));
        reportTypeComboBox.setPromptText("Select Report");
        controlsGrid.add(reportTypeComboBox, 1, 0);

        controlsGrid.add(new Label("Start Date:"), 0, 1);
        startDatePicker = new DatePicker();
        controlsGrid.add(startDatePicker, 1, 1);

        controlsGrid.add(new Label("End Date:"), 0, 2);
        endDatePicker = new DatePicker();
        controlsGrid.add(endDatePicker, 1, 2);

        generateReportButton = new Button("Generate Report");
        generateReportButton.setDefaultButton(true);
        // Using HBox for alignment, though not strictly necessary for a single button here
        HBox buttonBox = new HBox(generateReportButton);
        controlsGrid.add(buttonBox, 1, 3);

        controlsScrollPane = new ScrollPane(controlsGrid);
        controlsScrollPane.setFitToWidth(true);
        controlsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setTop(controlsScrollPane);

        // Center Area: Table
        this.setCenter(generatedReportsTable);
        BorderPane.setMargin(generatedReportsTable, new Insets(10, 0, 0, 0));

        // Setup and initial load
        setupGeneratedReportsTableColumns();
        setupEventListenersAndRefresh();
    }

    private void setupGeneratedReportsTableColumns() {
        generatedReportsTable.getColumns().clear();

        TableColumn<ReportMetadata, String> nameCol = new TableColumn<>("Report Name");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReportName()));
        nameCol.setPrefWidth(250);

        TableColumn<ReportMetadata, String> dateGenCol = new TableColumn<>("Date Generated");
        // Assuming getCreated() returns a String like "YYYY-MM-DD HH:MM:SS"
        // Might need formatting if it's a Timestamp or Date object
        dateGenCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCreated()));
        dateGenCol.setPrefWidth(150);

        TableColumn<ReportMetadata, String> formatCol = new TableColumn<>("Format");
        formatCol.setCellValueFactory(cellData -> {
            String path = cellData.getValue().getFilePath();
            String format = "N/A";
            if (path != null && path.contains(".")) {
                format = path.substring(path.lastIndexOf(".") + 1).toUpperCase();
            }
            return new SimpleStringProperty(format);
        });
        formatCol.setPrefWidth(80);

        TableColumn<ReportMetadata, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button openButton = new Button("Open");
            {
                openButton.setOnAction(event -> {
                    ReportMetadata reportMeta = getTableView().getItems().get(getIndex());
                    if (reportMeta != null && reportMeta.getFilePath() != null) {
                        try {
                            File reportFile = new File(reportMeta.getFilePath());
                            if (reportFile.exists()) {
                                if (Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().open(reportFile);
                                } else {
                                     new Alert(Alert.AlertType.WARNING, "Desktop operations not supported to open file.").showAndWait();
                                }
                            } else {
                                new Alert(Alert.AlertType.ERROR, "Report file not found: " + reportMeta.getFilePath()).showAndWait();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            new Alert(Alert.AlertType.ERROR, "Could not open report file: " + e.getMessage()).showAndWait();
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                            new Alert(Alert.AlertType.ERROR, "Desktop operations not supported on this platform (e.g. headless server).").showAndWait();
                        }
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Report path is not available.").showAndWait();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : openButton);
            }
        });
        actionsCol.setPrefWidth(100);

        generatedReportsTable.getColumns().addAll(nameCol, dateGenCol, formatCol, actionsCol);
    }

    private void loadGeneratedReports() {
        generatedReportsDataList.clear();
        // Assuming ReportService is configured to find reports correctly
        // or that reports are stored in a globally known location.
        // If ReportService needs a company context (like parent directory), it would be:
        // Company company = CurrentCompany.getCompany();
        // if (company != null && company.getParentFile() != null) {
        //    List<ReportMetadata> reports = reportService.listGeneratedReports(company.getParentFile());
        //    ...
        // } else { handle no company or no parent file }
        try {
             List<ReportMetadata> reports = reportService.listGeneratedReports(); // Using no-arg version
             if (reports != null) { // listGeneratedReports might return null on error
                generatedReportsDataList.addAll(reports);
             }
        } catch (Exception e) {
            System.err.println("Error loading generated reports: " + e.getMessage());
            e.printStackTrace(); // Good for debugging
            generatedReportsTable.setPlaceholder(new Label("Could not load generated reports: " + e.getMessage()));
        }

        if (generatedReportsDataList.isEmpty() && generatedReportsTable.getPlaceholder() instanceof Label) {
            // Only update placeholder if it's still the default one or an error one we set
             ((Label)generatedReportsTable.getPlaceholder()).setText("No generated reports found.");
        }
    }

    private void setupEventListenersAndRefresh() {
        companyChangeListener = new CompanyChangeListener() {
            @Override
            public void companyChange(boolean companyNowOpen) {
                // Report listing might be company-dependent if reports are stored within company folders
                // or if ReportService internals change based on current company.
                // For now, assuming listGeneratedReports() is global or handles CurrentCompany internally.
                loadGeneratedReports();
            }
        };
        CurrentCompany.CompanyListener.addCompanyListener(companyChangeListener);

        generateReportButton.setOnAction(e -> {
            String reportType = reportTypeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue(); // Can be null
            LocalDate endDate = endDatePicker.getValue();   // Can be null

            if (reportType == null || reportType.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please select a report type.").showAndWait();
                return;
            }
            // Date validation could be added here if dates are mandatory for all report types

            System.out.println("Generate Report clicked for: " + reportType +
                               (startDate != null ? " from " + startDate : "") +
                               (endDate != null ? " to " + endDate : "") +
                               " - Placeholder action.");

            // TODO: Implement actual report generation logic using ReportService
            // This would involve:
            // 1. Getting Company context (e.g., company.getParentFile() for report path, CurrentCompany.getCompany())
            // 2. Calling appropriate method on reportService (e.g., reportService.generateIncomeStatement(CurrentCompany.getCompany(), startDate, endDate))
            // 3. After generation, call loadGeneratedReports() to refresh the list.

            // For now, just show an alert.
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "Report generation for '" + reportType + "' is a placeholder and not yet fully implemented.");
            infoAlert.setHeaderText("Feature Not Implemented");
            infoAlert.showAndWait();

            loadGeneratedReports(); // Refresh list to see if anything changed (e.g. if user manually added a report)
        });

        loadGeneratedReports(); // Initial data load
    }

    // ReportInfo inner class is removed
}
