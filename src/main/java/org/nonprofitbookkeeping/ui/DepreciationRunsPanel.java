package org.nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nonprofitbookkeeping.service.DepreciationRunLifecycleService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents the DepreciationRunsPanel component in the nonprofit bookkeeping application.
 */
public class DepreciationRunsPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final DepreciationRunLifecycleService lifecycleService;

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

        Button run = new Button("Run Depreciation");
        Button preview = new Button("Preview Journal");
        run.setOnAction(evt -> runDepreciation());
        preview.setOnAction(evt -> showPreviewHint());
        HBox actions = new HBox(8, run, preview);

        root.setTop(new VBox(6, title, actions, new Separator()));
        root.setCenter(new Label("TODO: Depreciation run wizard + posting preview (outputs + automation)."));
    }

    @Override public String title() { return "Depreciation Runs"; }
    @Override public Node root() { return root; }

    private void runDepreciation()
    {
        LocalDate today = LocalDate.now();
        LocalDate periodStart = today.withDayOfMonth(1);
        LocalDate periodEnd = today;
        String runId = "ui-run-" + UUID.randomUUID();
        try
        {
            lifecycleService.createDraftRun(runId, periodStart, periodEnd, "Created from depreciation panel");
            lifecycleService.transitionStatus(runId, "CALCULATED", null, "ui-user", "Generated from panel action");
            showInfo("Depreciation run created",
                "Run " + runId + " created for " + periodStart + " to " + periodEnd + " and marked CALCULATED.");
        }
        catch (SQLException | RuntimeException ex)
        {
            showError("Depreciation run failed", ex.getMessage());
        }
    }

    private void showPreviewHint()
    {
        showInfo("Preview Journal",
            "Journal preview wiring is next; lifecycle service is now connected for run creation.");
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
}
