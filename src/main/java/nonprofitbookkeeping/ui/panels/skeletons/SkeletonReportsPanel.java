package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent; // Added import
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
import javafx.scene.control.Alert;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.CurrentCompany.CompanyChangeListener;
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.ui.actions.GenerateBalanceSheetAction; // Added import
import nonprofitbookkeeping.ui.actions.GenerateCashFlowStatementAction; // Added import
import nonprofitbookkeeping.ui.actions.GenerateIncomeStatementAction; // Added import
import nonprofitbookkeeping.ui.actions.GenerateTrialBalanceAction; // Added import
import nonprofitbookkeeping.ui.helpers.AlertBox; // Added import for AlertBox

import java.util.List;
// import java.util.ArrayList; // No longer strictly needed with current code
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

        generateReportButton.setOnAction(event -> { // event is an ActionEvent from the button click
            String reportTypeKey = reportTypeComboBox.getValue(); // This is the display name
            Company currentCompany = CurrentCompany.getCompany();

            if (currentCompany == null) {
                AlertBox.showError(this.getScene().getWindow(), "No company is currently open.");
                return;
            }
            if (reportTypeKey == null) {
                AlertBox.showError(this.getScene().getWindow(), "Please select a report type.");
                return;
            }

            ActionEvent newEvent = new ActionEvent(generateReportButton, null);

            try {
                if ("Income Statement".equals(reportTypeKey)) {
                    new GenerateIncomeStatementAction(this.reportService).handle(newEvent);
                } else if ("Balance Sheet".equals(reportTypeKey)) {
                    new GenerateBalanceSheetAction(this.reportService).handle(newEvent);
                } else if ("Trial Balance".equals(reportTypeKey)) {
                    new GenerateTrialBalanceAction(this.reportService).handle(newEvent);
                } else if ("Cash Flow Statement".equals(reportTypeKey)) {
                    new GenerateCashFlowStatementAction(this.reportService).handle(newEvent);
                } else {
                    AlertBox.showError(this.getScene().getWindow(), "Report type '" + reportTypeKey + "' generation not implemented yet.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertBox.showError(this.getScene().getWindow(), "Error generating report: " + ex.getMessage());
            } finally {
                loadGeneratedReports(); // Refresh the list of generated reports
            }
        });

        loadGeneratedReports();
    }
}
