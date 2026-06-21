package org.nonprofitbookkeeping.ui.panels;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.nonprofitbookkeeping.bridge.dashboard.DashboardDataBridge;
import org.nonprofitbookkeeping.service.FundBalanceRow;
import org.nonprofitbookkeeping.ui.UiAsync;
import org.nonprofitbookkeeping.ui.UiErrors;
import org.nonprofitbookkeeping.ui.UiSpacing;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

/**
 * Imported-style Dashboard panel retained under original package name.
 * Public API kept constructor-only, with local bridge-backed data wiring.
 */
public class DashboardPanelFX extends BorderPane
{
    private final Label companyLbl = new Label("Current data context");
    private final Button reloadBtn = new Button("Reload");
    private final ComboBox<String> accountSelector = new ComboBox<>();
    private final TextField memoFilter = new TextField();
    private final TextField amountFilter = new TextField();

    private final TableView<Row> table = new TableView<>();
    private final DashboardDataBridge bridge = new DashboardDataBridge();

    public DashboardPanelFX()
    {
        setPadding(UiSpacing.pageInsets());
        buildTopBanner();
        buildTopFilters();
        buildTable();
        setCenter(new TitledPane("Fund dashboard rows", this.table) {{ setCollapsible(false); }});
        this.reloadBtn.setOnAction(e -> refresh());
        refresh();
    }

    private void buildTopBanner()
    {
        HBox banner = new HBox(UiSpacing.SECTION_SPACING, new Label("Context:"), this.companyLbl, this.reloadBtn);
        banner.setPadding(new Insets(UiSpacing.SECTION_SPACING));
        banner.getStyleClass().add("dashboard-banner");
        setTop(banner);
    }

    private void buildTopFilters()
    {
        this.accountSelector.setPromptText("Filter by fund code");
        this.memoFilter.setPromptText("Filter by fund name");
        this.amountFilter.setPromptText("Min balance");
        Button apply = new Button("Apply");
        apply.setOnAction(e -> refresh());

        HBox filterBox = new HBox(UiSpacing.SECTION_SPACING,
            new Label("Fund:"), this.accountSelector,
            new Label("Name:"), this.memoFilter,
            new Label("Min:"), this.amountFilter,
            apply);
        filterBox.setPadding(new Insets(UiSpacing.SECTION_SPACING));
        filterBox.getStyleClass().add("dashboard-filter-box");

        Node currentTop = getTop();
        VBox topControls = new VBox(currentTop, filterBox);
        setTop(topControls);
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private void buildTable()
    {
        TableColumn<Row, Object> codeCol = mkCol("Fund", r -> r.fundCode);
        TableColumn<Row, Object> nameCol = mkCol("Name", r -> r.fundName);
        TableColumn<Row, Object> balCol = mkCol("Balance", r -> r.balance);
        this.table.getColumns().addAll(codeCol, nameCol, balCol);
        this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private static <T> TableColumn<Row, T> mkCol(String n, Function<Row, T> f)
    {
        TableColumn<Row, T> c = new TableColumn<>(n);
        c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(f.apply(cd.getValue())));
        return c;
    }

    public void reloadData()
    {
        refresh();
    }

    private void refresh()
    {
        this.reloadBtn.setDisable(true);
        UiAsync.run("imported-dashboard-load",
            this.bridge::load,
            snapshot -> {
                String selected = this.accountSelector.getValue();
                this.accountSelector.getItems().setAll(snapshot.rows().stream().map(FundBalanceRow::getFundCode).filter(Objects::nonNull).sorted().toList());
                if (selected != null && this.accountSelector.getItems().contains(selected)) {
                    this.accountSelector.setValue(selected);
                }

                String nameLike = this.memoFilter.getText() == null ? "" : this.memoFilter.getText().trim().toLowerCase();
                BigDecimal min = parseMin(this.amountFilter.getText());
                String fundCode = this.accountSelector.getValue();

                this.table.getItems().setAll(snapshot.rows().stream()
                    .filter(r -> fundCode == null || fundCode.isBlank() || fundCode.equals(r.getFundCode()))
                    .filter(r -> {
                        String fundName = r.getFundName() == null ? "" : r.getFundName();
                        return nameLike.isBlank() || fundName.toLowerCase().contains(nameLike);
                    })
                    .filter(r -> min == null || r.getBalance().compareTo(min) >= 0)
                    .map(r -> new Row(r.getFundCode(), r.getFundName(), r.getBalance().toPlainString()))
                    .toList());

                this.companyLbl.setText("Funds=" + snapshot.fundCount() + " | Accounts=" + snapshot.accountCount());
                this.reloadBtn.setDisable(false);
            },
            ex -> {
                this.companyLbl.setText("Dashboard error: " + UiErrors.safeMessage(ex));
                this.reloadBtn.setDisable(false);
            });
    }

    private BigDecimal parseMin(String value)
    {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record Row(String fundCode, String fundName, String balance) {}
}
