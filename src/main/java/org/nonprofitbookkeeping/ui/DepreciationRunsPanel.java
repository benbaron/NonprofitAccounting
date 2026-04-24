package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.service.DepreciationRunLifecycleService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents the DepreciationRunsPanel component in the nonprofit bookkeeping application.
 */
public class DepreciationRunsPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final DepreciationRunLifecycleService lifecycleService;
    private final DatePicker periodStartPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker periodEndPicker = new DatePicker(LocalDate.now());
    private final TextArea notesArea = new TextArea("Created from depreciation panel");
    private final TableView<RunRow> runsTable = new TableView<>();
    private final Label status = new Label("Ready");

    public DepreciationRunsPanel()
    {
        this(new DepreciationRunLifecycleService());
    }

    DepreciationRunsPanel(DepreciationRunLifecycleService lifecycleService)
    {
        this.lifecycleService = lifecycleService;
        root.setPadding(new Insets(8));
        Label title = new Label("Depreciation Runs");
        title.getStyleClass().add("panel-title");

        notesArea.setPrefRowCount(2);
        notesArea.setWrapText(true);

        GridPane inputs = new GridPane();
        inputs.setHgap(8);
        inputs.setVgap(6);
        inputs.addRow(0, new Label("Period Start"), periodStartPicker);
        inputs.addRow(1, new Label("Period End"), periodEndPicker);
        inputs.addRow(2, new Label("Notes"), notesArea);

        Button run = new Button("Create Draft Run");
        Button calculate = new Button("Mark Calculated");
        Button preview = new Button("Preview Journal");
        Button lock = new Button("Lock Selected");
        run.setOnAction(evt -> createDraftRun());
        calculate.setOnAction(evt -> markSelectedCalculated());
        preview.setOnAction(evt -> showPreviewHint());
        lock.setOnAction(evt -> lockSelectedRun());
        HBox actions = new HBox(8, run, calculate, lock, preview);

        root.setTop(new VBox(6, title, actions, new Separator()));
        root.setCenter(new VBox(8, inputs, new Separator(), configureRunsTable(), status));
    }

    @Override public String title() { return "Depreciation Runs"; }
    @Override public Node root() { return root; }

    private TableView<RunRow> configureRunsTable()
    {
        runsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        runsTable.getColumns().add(column("Run ID", "runId"));
        runsTable.getColumns().add(column("Start", "periodStart"));
        runsTable.getColumns().add(column("End", "periodEnd"));
        runsTable.getColumns().add(column("Status", "status"));
        runsTable.getColumns().add(column("Locked", "locked"));
        runsTable.getColumns().add(column("Last Updated", "lastUpdated"));
        return runsTable;
    }

    private TableColumn<RunRow, String> column(String title, String property)
    {
        TableColumn<RunRow, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private void createDraftRun()
    {
        LocalDate periodStart = periodStartPicker.getValue();
        LocalDate periodEnd = periodEndPicker.getValue();
        if (periodStart == null || periodEnd == null || periodEnd.isBefore(periodStart))
        {
            showError("Invalid period", "Choose a valid start/end period for the run.");
            return;
        }
        String runId = "ui-run-" + UUID.randomUUID();
        String notes = notesArea.getText() == null ? "" : notesArea.getText().trim();
        try
        {
            lifecycleService.createDraftRun(runId, periodStart, periodEnd, notes.isEmpty() ? "Created from panel" : notes);
            runsTable.getItems().add(new RunRow(runId, periodStart.toString(), periodEnd.toString(),
                "DRAFT", "No", nowStamp()));
            status.setText("Draft run created: " + runId);
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Depreciation run failed", ex.getMessage());
        }
    }

    private void markSelectedCalculated()
    {
        RunRow row = runsTable.getSelectionModel().getSelectedItem();
        if (row == null)
        {
            showError("No run selected", "Select a draft run first.");
            return;
        }
        if ("Yes".equals(row.getLocked()))
        {
            showError("Run locked", "Locked runs cannot be changed.");
            return;
        }
        try
        {
            lifecycleService.transitionStatus(row.getRunId(), "CALCULATED", null, "ui-user",
                "Calculated from depreciation panel");
            row.setStatus("CALCULATED");
            row.setLastUpdated(nowStamp());
            runsTable.refresh();
            status.setText("Run marked CALCULATED: " + row.getRunId());
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Could not update run", ex.getMessage());
        }
    }

    private void lockSelectedRun()
    {
        RunRow row = runsTable.getSelectionModel().getSelectedItem();
        if (row == null)
        {
            showError("No run selected", "Select a calculated run first.");
            return;
        }
        try
        {
            lifecycleService.lockRun(row.getRunId(), "ui-reviewer", "Locked from depreciation panel");
            row.setLocked("Yes");
            row.setStatus("LOCKED");
            row.setLastUpdated(nowStamp());
            runsTable.refresh();
            status.setText("Run locked: " + row.getRunId());
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Could not lock run", ex.getMessage());
        }
    }

    private void showPreviewHint()
    {
        RunRow row = runsTable.getSelectionModel().getSelectedItem();
        if (row == null)
        {
            showError("No run selected", "Select a run to preview generated journal details.");
            return;
        }
        showInfo("Preview Journal",
            "Preview for " + row.getRunId() + " (" + row.getStatus() + ") is the next integration step.");
    }

    private String nowStamp()
    {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void showInfo(String title, String content)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content == null ? "Unknown error." : content);
        alert.showAndWait();
    }

    public static final class RunRow
    {
        private String runId;
        private String periodStart;
        private String periodEnd;
        private String status;
        private String locked;
        private String lastUpdated;

        public RunRow(String runId, String periodStart, String periodEnd, String status, String locked, String lastUpdated)
        {
            this.runId = runId;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.status = status;
            this.locked = locked;
            this.lastUpdated = lastUpdated;
        }

        public String getRunId() { return runId; }
        public String getPeriodStart() { return periodStart; }
        public String getPeriodEnd() { return periodEnd; }
        public String getStatus() { return status; }
        public String getLocked() { return locked; }
        public String getLastUpdated() { return lastUpdated; }
        public void setStatus(String status) { this.status = status; }
        public void setLocked(String locked) { this.locked = locked; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
