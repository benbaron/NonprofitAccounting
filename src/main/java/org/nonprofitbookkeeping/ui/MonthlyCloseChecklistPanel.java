package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/** Native alternate monthly close / exchequer checklist panel. */
public class MonthlyCloseChecklistPanel implements AppPanel
{
    private final AlternatePanelScaffold scaffold = new AlternatePanelScaffold("Monthly Close Checklist");
    private final MonthlyCloseChecklistService service;
    private final DatePicker periodEnd = new DatePicker(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
    private final TableView<MonthlyCloseChecklistService.CloseChecklistItem> table = new TableView<>();

    public MonthlyCloseChecklistPanel(UiServiceProvider services)
    {
        this(new MonthlyCloseChecklistService(services));
    }

    MonthlyCloseChecklistPanel(MonthlyCloseChecklistService service)
    {
        this.service = service;
        build();
        refresh();
    }

    public String title() { return "Monthly Close Checklist"; }
    public Node root() { return this.scaffold; }

    private void build()
    {
        this.scaffold.setSubtitle("Guide the branch exchequer through service-backed close checks without inventing completion statuses.");
        Button refresh = new Button("Refresh Checks");
        refresh.setOnAction(e -> refresh());
        this.scaffold.setPrimaryActions(List.of(refresh));
        this.scaffold.setFilterBar(new HBox(8, new Label("Period end"), this.periodEnd));

        TableColumn<MonthlyCloseChecklistService.CloseChecklistItem, String> step = new TableColumn<>("Step");
        step.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().label()));
        step.setPrefWidth(220);
        TableColumn<MonthlyCloseChecklistService.CloseChecklistItem, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(data -> new SimpleStringProperty(label(data.getValue().status())));
        status.setPrefWidth(150);
        TableColumn<MonthlyCloseChecklistService.CloseChecklistItem, String> detail = new TableColumn<>("Service-backed detail / not-wired state");
        detail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().detail()));
        detail.setPrefWidth(620);
        this.table.getColumns().setAll(step, status, detail);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Label note = new Label("Use the linked workspaces to reconcile accounts, review deposits/imports, verify fund balances, generate reports, back up the database, and close/lock only where a real service supports it.");
        note.setWrapText(true);
        VBox content = new VBox(10, note, this.table);
        content.setPadding(new Insets(12));
        this.scaffold.setContent(content);
    }

    private void refresh()
    {
        MonthlyCloseChecklistService.CloseChecklistState state = this.service.calculate(this.periodEnd.getValue());
        this.table.getItems().setAll(state.items());
        this.scaffold.setStatus("Checklist calculated for period ending " + state.periodEnd() + ". Not-wired means no service-backed check exists yet.");
    }

    private static String label(MonthlyCloseChecklistService.ChecklistStatus status)
    {
        return switch (status)
        {
            case COMPLETE -> "Complete";
            case ACTION_REQUIRED -> "Action required";
            case BLOCKED -> "Blocked";
            case NOT_WIRED -> "Not wired";
        };
    }
}
