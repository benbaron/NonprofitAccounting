package nonprofitbookkeeping.ui.panels.skeletons;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox; // Though not explicitly asked for bottom, it's good for consistency if needed later

public class SkeletonReportsPanel extends BorderPane {

    public SkeletonReportsPanel() {
        setPadding(new Insets(15)); // Overall padding

        // Report Generation Controls (Top)
        GridPane controlsGrid = new GridPane();
        controlsGrid.setPadding(new Insets(10));
        controlsGrid.setHgap(10);
        controlsGrid.setVgap(10);

        controlsGrid.add(new Label("Report Type:"), 0, 0);
        ComboBox<String> reportTypeCombo = new ComboBox<>();
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Income Statement", "Balance Sheet", "Trial Balance", "Cash Flow Statement"));
        reportTypeCombo.setPromptText("Select Report");
        controlsGrid.add(reportTypeCombo, 1, 0);

        controlsGrid.add(new Label("Start Date:"), 0, 1);
        DatePicker startDatePicker = new DatePicker();
        controlsGrid.add(startDatePicker, 1, 1);

        controlsGrid.add(new Label("End Date:"), 0, 2);
        DatePicker endDatePicker = new DatePicker();
        controlsGrid.add(endDatePicker, 1, 2);

        Button generateButton = new Button("Generate Report");
        generateButton.setDefaultButton(true);
        HBox buttonBox = new HBox(generateButton); // To align button to right if needed or add more
        controlsGrid.add(buttonBox, 1, 3);

        this.setTop(controlsGrid);

        // Generated Reports List (Center)
        TableView<ReportInfo> reportsTable = new TableView<>();
        reportsTable.setPlaceholder(new Label("No reports generated yet."));

        TableColumn<ReportInfo, String> nameCol = new TableColumn<>("Report Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("reportName"));
        nameCol.setPrefWidth(250);

        TableColumn<ReportInfo, String> dateGenCol = new TableColumn<>("Date Generated");
        dateGenCol.setCellValueFactory(new PropertyValueFactory<>("dateGenerated"));
        dateGenCol.setPrefWidth(150);

        TableColumn<ReportInfo, String> formatCol = new TableColumn<>("Format");
        formatCol.setCellValueFactory(new PropertyValueFactory<>("format"));
        formatCol.setPrefWidth(100);

        TableColumn<ReportInfo, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button openButton = new Button("Open");
            {
                openButton.setOnAction(event -> {
                    ReportInfo report = getTableView().getItems().get(getIndex());
                    System.out.println("Opening report: " + report.getReportName() + " (" + report.getFormat() + ")");
                    // Actual open logic would go here
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(openButton);
                }
            }
        });
        actionsCol.setPrefWidth(100);


        reportsTable.getColumns().addAll(nameCol, dateGenCol, formatCol, actionsCol);

        ObservableList<ReportInfo> data = FXCollections.observableArrayList(
                new ReportInfo("Income Statement Q3 2023", "2023-10-01", "PDF"),
                new ReportInfo("Balance Sheet Sept 2023", "2023-10-01", "XLSX"),
                new ReportInfo("Trial Balance YTD Oct 2023", "2023-10-27", "PDF")
        );
        reportsTable.setItems(data);
        this.setCenter(reportsTable);
        BorderPane.setMargin(reportsTable, new Insets(10, 0, 0, 0)); // Margin above table
    }

    public static class ReportInfo {
        private final SimpleStringProperty reportName;
        private final SimpleStringProperty dateGenerated;
        private final SimpleStringProperty format;

        public ReportInfo(String reportName, String dateGenerated, String format) {
            this.reportName = new SimpleStringProperty(reportName);
            this.dateGenerated = new SimpleStringProperty(dateGenerated);
            this.format = new SimpleStringProperty(format);
        }

        public String getReportName() { return reportName.get(); }
        public SimpleStringProperty reportNameProperty() { return reportName; }

        public String getDateGenerated() { return dateGenerated.get(); }
        public SimpleStringProperty dateGeneratedProperty() { return dateGenerated; }

        public String getFormat() { return format.get(); }
        public SimpleStringProperty formatProperty() { return format; }
    }
}
