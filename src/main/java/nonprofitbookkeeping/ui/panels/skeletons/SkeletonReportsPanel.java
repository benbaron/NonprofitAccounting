package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
// import javafx.event.ActionEvent; // No longer needed with lambda 'event ->'
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
// import javafx.scene.control.Alert; // Replaced by AlertBox
import javafx.stage.Window; // Added for getScene().getWindow()

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.reports.ReportContext; // Added import
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.service.ReportService;
// Action class imports are no longer directly needed in this event handler
// import nonprofitbookkeeping.ui.actions.GenerateBalanceSheetAction;
// import nonprofitbookkeeping.ui.actions.GenerateCashFlowStatementAction;
// import nonprofitbookkeeping.ui.actions.GenerateIncomeStatementAction;
// import nonprofitbookkeeping.ui.actions.GenerateTrialBalanceAction;
import nonprofitbookkeeping.ui.helpers.AlertBox;

import java.util.List;
import java.io.File;
import java.awt.Desktop;
import java.io.IOException;
import java.time.LocalDate;

public class SkeletonReportsPanel extends BorderPane {

    private ComboBox<String> reportTypeComboBox;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Button generateReportButton;
    private TableView<ReportMetadata> generatedReportsTable;
    private ObservableList<ReportMetadata> generatedReportsDataList;

    private ReportService reportService;
    private CompanyChangeListener companyChangeListener;

    private GridPane controlsGrid;
    private ScrollPane controlsScrollPane;


    public SkeletonReportsPanel() {
        setPadding(new Insets(15));
        reportService = new ReportService();

        generatedReportsDataList = FXCollections.observableArrayList();
        generatedReportsTable = new TableView<>(generatedReportsDataList);
        generatedReportsTable.setPlaceholder(new Label("No reports found or company not open."));

        controlsGrid = new GridPane();
        controlsGrid.setPadding(new Insets(10));
        controlsGrid.setHgap(10);
        controlsGrid.setVgap(10);

        controlsGrid.add(new Label("Report Type:"), 0, 0);
        reportTypeComboBox = new ComboBox<>();
        // Ensure "Balance Sheet" and "Trial Balance" are included for the new logic
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Income Statement", "Balance Sheet", "Trial Balance", "Cash Flow Statement"
                // Add other JXLS reports here if they are to be handled by a different mechanism
                // e.g., "Budget vs. Actuals"
        ));
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
        HBox buttonBox = new HBox(generateReportButton);
        controlsGrid.add(buttonBox, 1, 3);

        controlsScrollPane = new ScrollPane(controlsGrid);
        controlsScrollPane.setFitToWidth(true);
        controlsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        controlsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setTop(controlsScrollPane);

        this.setCenter(generatedReportsTable);
        BorderPane.setMargin(generatedReportsTable, new Insets(10, 0, 0, 0));

        setupGeneratedReportsTableColumns();
        setupEventListenersAndRefresh();
    }

    private void setupGeneratedReportsTableColumns() {
        generatedReportsTable.getColumns().clear();

        TableColumn<ReportMetadata, String> nameCol = new TableColumn<>("Report Name");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReportName()));
        nameCol.setPrefWidth(250);

        TableColumn<ReportMetadata, String> dateGenCol = new TableColumn<>("Date Generated");
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
                                     AlertBox.showError(getScene().getWindow(), "Desktop operations not supported to open file.");
                                }
                            } else {
                                AlertBox.showError(getScene().getWindow(), "Report file not found: " + reportMeta.getFilePath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            AlertBox.showError(getScene().getWindow(), "Could not open report file: " + e.getMessage());
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                            AlertBox.showError(getScene().getWindow(), "Desktop operations not supported on this platform (e.g. headless server).");
                        }
                    } else {
                        AlertBox.showWarning(getScene().getWindow(), "Report path is not available.");
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
        try {
             List<ReportMetadata> reports = reportService.listGeneratedReports();
             if (reports != null) {
                generatedReportsDataList.addAll(reports);
             }
        } catch (Exception e) {
            System.err.println("Error loading generated reports: " + e.getMessage());
            e.printStackTrace();
            generatedReportsTable.setPlaceholder(new Label("Could not load generated reports: " + e.getMessage()));
        }

        if (generatedReportsDataList.isEmpty() && generatedReportsTable.getPlaceholder() instanceof Label) {
             ((Label)generatedReportsTable.getPlaceholder()).setText("No generated reports found.");
        }
    }

    private void setupEventListenersAndRefresh() {
        companyChangeListener = new CompanyChangeListener() {
            @Override
            public void companyChange(boolean companyNowOpen) {
                loadGeneratedReports();
            }
        };
        CurrentCompany.CompanyListener.addCompanyListener(companyChangeListener);

        generateReportButton.setOnAction(event -> {
            String reportTypeDisplay = reportTypeComboBox.getValue();
            Company currentCompany = CurrentCompany.getCompany();
            Window ownerWindow = this.getScene().getWindow();

            if (currentCompany == null) {
                AlertBox.showError(ownerWindow, "No company is currently open.");
                return;
            }
            if (reportTypeDisplay == null) {
                AlertBox.showError(ownerWindow, "Please select a report type.");
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            String reportTypeKey;
            boolean isJasperReport = true;

            switch (reportTypeDisplay) {
                case "Income Statement":
                    reportTypeKey = "income_statement_jasper";
                    break;
                case "Balance Sheet":
                    reportTypeKey = "balance_sheet_jasper";
                    // AlertBox.showInfo(ownerWindow, "Balance Sheet via Jasper is chosen, but ensure its generator is fully implemented in ReportService.");
                    break;
                case "Trial Balance":
                    reportTypeKey = "trial_balance_jasper";
                    // AlertBox.showInfo(ownerWindow, "Trial Balance via Jasper is chosen, but ensure its generator is fully implemented in ReportService.");
                    break;
                case "Cash Flow Statement":
                    reportTypeKey = "cash_flow_statement_jasper";
                    break;
                default:
                    AlertBox.showError(ownerWindow, "Report type '" + reportTypeDisplay + "' generation not configured for Jasper system.");
                    return;
            }

            ReportContext ctx = new ReportContext();
            ctx.setReportType(reportTypeKey);
            ctx.setStartDate(startDate);
            ctx.setEndDate(endDate);
            // TODO: ctx.setFundIds(...);
            // TODO: ctx.setSelectedBudget(...);
            // TODO: ctx.setAccountIdsForDetailReport(...);

            String outputFormat = "pdf";

            if (isJasperReport) {
                if (("income_statement_jasper".equals(reportTypeKey) || "cash_flow_statement_jasper".equals(reportTypeKey))
                    && (startDate == null || endDate == null)) {
                    AlertBox.showError(ownerWindow, "Please select both a Start Date and End Date for this report.");
                    return;
                }
                if (("balance_sheet_jasper".equals(reportTypeKey) || "trial_balance_jasper".equals(reportTypeKey))
                    && endDate == null) {
                    AlertBox.showError(ownerWindow, "Please select an End Date (As-Of Date) for this report.");
                    return;
                }
                if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                    AlertBox.showError(ownerWindow, "End Date cannot be before Start Date.");
                    return;
                }

                try {
                    File generatedFile = this.reportService.generateJasperReport(ctx, outputFormat);

                    if (generatedFile != null && generatedFile.exists()) {
                        AlertBox.showInfo(ownerWindow, reportTypeDisplay + " generated: " + generatedFile.getAbsolutePath());
                        try {
                            if (Desktop.isDesktopSupported()) { // Check if Desktop API is supported
                                Desktop.getDesktop().open(generatedFile);
                            } else {
                                AlertBox.showWarning(ownerWindow, "Cannot automatically open file. Desktop operations not supported on this platform. File saved at: " + generatedFile.getAbsolutePath());
                            }
                        } catch (IOException | UnsupportedOperationException ex) {
                            ex.printStackTrace();
                            AlertBox.showError(ownerWindow, "Could not open report file: " + ex.getMessage() +
                                               (ex instanceof UnsupportedOperationException ? "\nDesktop operations not supported on this platform." : ""));
                        }
                    } else {
                        AlertBox.showError(ownerWindow, reportTypeDisplay + " could not be generated or found. Check console/logs.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    AlertBox.showError(ownerWindow, "Error generating " + reportTypeDisplay + ": " + ex.getMessage());
                } finally {
                    loadGeneratedReports();
                }
            } else {
                // This block would handle non-Jasper reports if any were configured
                AlertBox.showInfo(ownerWindow, "Generation for non-Jasper report type '" + reportTypeDisplay + "' is not handled by this path.");
            }
        });

        loadGeneratedReports();
    }
}
