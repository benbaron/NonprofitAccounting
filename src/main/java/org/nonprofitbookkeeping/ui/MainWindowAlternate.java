package org.nonprofitbookkeeping.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.nonprofitbookkeeping.ui.alternate.AlternateChartOfAccountsView;
import org.nonprofitbookkeeping.ui.alternate.AlternateInventoryTransferOrdersView;
import org.nonprofitbookkeeping.ui.alternate.AlternateManualJournalView;
import org.nonprofitbookkeeping.ui.alternate.AlternateReportsOverviewView;
import org.nonprofitbookkeeping.ui.routing.WorkspaceRouteDecision;
import org.nonprofitbookkeeping.ui.routing.WorkspaceRouter;

/**
 * Alternate dashboard-first UI shell that preserves current panel APIs.
 */
public class MainWindowAlternate extends BorderPane
{
    private static final Map<String, Color> SURFACE_COLORS = Map.of(
        "Slate", Color.web("#eef1f8"),
        "Warm", Color.web("#f7f2ee"),
        "Cool", Color.web("#edf5f9"));

    private final PanelHost panelHost = new PanelHost();
    private final NavigationPane nav =
        new NavigationPane(this::openPanel, this::openInspectorForSelection, this::openRecordServicePanel);
    private final Label alternateStatus = new Label("Select an item to see context details.");
    private final VBox dashboardCanvas = new VBox();
    private final StackPane workspaceSurface = new StackPane();
    private final VBox alternateSettingsPane = new VBox();
    private final StackPane alternateContentPane = new StackPane();
    private final VBox databaseSelectorPane = new VBox();
    private final VBox companySelectorPane = new VBox();
    private final WorkspaceRouter workspaceRouter = new WorkspaceRouter();

    public MainWindowAlternate()
    {
        setTop(buildHeader());
        setCenter(buildWorkspace());
        setLeft(buildIconRail());
        setPadding(new Insets(10));
        setBackground(new Background(new BackgroundFill(SURFACE_COLORS.get("Slate"), CornerRadii.EMPTY, Insets.EMPTY)));
        openPanel(AppPanelId.DASHBOARD);
    }

    private Node buildIconRail()
    {
        VBox rail = new VBox(14, iconButton("◉"), iconButton("⌂"), iconButton("⌕"), iconButton("⚙"));
        rail.setPadding(new Insets(14, 8, 14, 8));
        rail.setStyle("-fx-background-color: #1f2431; -fx-background-radius: 14;");
        return rail;
    }

    private Button iconButton(String text)
    {
        Button button = new Button(text);
        button.setMinSize(46, 46);
        button.setStyle("-fx-background-color: #2c3347; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 20px;");
        return button;
    }

    private Node buildHeader()
    {
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 700;");
        Label subtitle = new Label("San Crescent Accounting");
        subtitle.setStyle("-fx-text-fill: #5c6482;");

        VBox heading = new VBox(2, title, subtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ChoiceBox<String> period = new ChoiceBox<>(FXCollections.observableArrayList("This Month", "Quarter", "Year"));
        period.setValue("This Month");
        DateRangeSelector rangeSelector = new DateRangeSelector();

        HBox header = new HBox(12, heading, spacer, new Label("Range"), period, rangeSelector);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        return header;
    }

    private Node buildWorkspace()
    {
        VBox leftNav = buildLeftNavigation();

        dashboardCanvas.getChildren().setAll(buildDashboardCards());
        dashboardCanvas.setSpacing(12);

        panelHost.setVisible(false);
        panelHost.setManaged(false);

        alternateSettingsPane.setVisible(false);
        alternateSettingsPane.setManaged(false);
        alternateContentPane.setVisible(false);
        alternateContentPane.setManaged(false);
        workspaceSurface.getChildren().setAll(dashboardCanvas, alternateSettingsPane, alternateContentPane, panelHost);
        StackPane.setMargin(dashboardCanvas, new Insets(8));
        StackPane.setMargin(panelHost, new Insets(8));
        HBox.setHgrow(workspaceSurface, Priority.ALWAYS);
        workspaceSurface.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 18;");

        HBox body = new HBox(12, leftNav, workspaceSurface);
        body.setAlignment(Pos.TOP_LEFT);
        return body;
    }

    private VBox buildLeftNavigation()
    {
        VBox navButtons = new VBox(6,
            navButton("⌂  Dashboard", AppPanelId.DASHBOARD),
            navButton("🗂  Chart of Accounts", AppPanelId.CHART_OF_ACCOUNTS),
            navButton("🧾  Journal", AppPanelId.LEDGER_REGISTER),
            navButton("📦  Inventory", AppPanelId.INVENTORY),
            navButton("💰  Funds", AppPanelId.FUNDS),
            navButton("📊  Reports", AppPanelId.REPORTS_WORKSPACE),
            navButton("🗓  Schedules", AppPanelId.SCHEDULES),
            navButton("📈  Budget", AppPanelId.BUDGET_EDITOR),
            navButton("⚙  Settings", AppPanelId.SETTINGS),
            navActionButton("🗄  Open Database", this::openDatabaseSelector),
            navActionButton("🏢  Open Company", this::openCompanySelector));

        TitledPane imports = new TitledPane("Import & Tools", new VBox(6,
            navButton("Assets Register", AppPanelId.ASSETS_REGISTER),
            navButton("Budget vs Actual", AppPanelId.BUDGET_VS_ACTUAL),
            navButton("Depreciation", AppPanelId.DEPRECIATION_RUNS)));
        imports.setExpanded(false);

        VBox wrapper = new VBox(10, new Label("Navigation"), navButtons, imports);
        wrapper.setPadding(new Insets(12));
        wrapper.setMinWidth(240);
        wrapper.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16;");
        return wrapper;
    }

    private Button navButton(String label, AppPanelId id)
    {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> openPanel(id));
        return button;
    }


    private Button navActionButton(String label, Runnable action)
    {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Node buildDashboardCards()
    {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(buildCard("Receivables", "$11,230", "+8.2%"), 0, 0);
        grid.add(buildCard("Payables", "$5,830", "-1.1%"), 1, 0);
        grid.add(buildCard("Profit & Loss", "$23,009", "+14.5%"), 2, 0);
        grid.add(buildChartCard(), 0, 1, 2, 1);
        grid.add(buildBalancesCard(), 2, 1);
        return new ScrollPane(grid);
    }

    private VBox buildCard(String title, String value, String delta)
    {
        Label t = new Label(title);
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 20px; -fx-font-weight: 700;");
        Label d = new Label(delta);
        d.setStyle("-fx-text-fill: #5962cc;");
        VBox box = new VBox(8, t, v, d);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return box;
    }

    private VBox buildChartCard()
    {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 9));
        series.getData().add(new XYChart.Data<>("Feb", 12));
        series.getData().add(new XYChart.Data<>("Mar", 11));
        series.getData().add(new XYChart.Data<>("Apr", 15));
        chart.getData().add(series);

        VBox box = new VBox(8, new Label("Cash Flow"), chart);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return box;
    }

    private VBox buildBalancesCard()
    {
        VBox list = new VBox(6,
            new Label("Operating  ·  $12,004"),
            new Label("Payroll      ·  $3,420"),
            new Label("Savings      ·  $7,230"),
            new Label("Undeposited  ·  $980"));
        VBox box = new VBox(8, new Label("Account Balances"), new Separator(), list);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return box;
    }

    private VBox buildAlternateSettingsPane()
    {
        ChoiceBox<String> backgroundChoice = new ChoiceBox<>(FXCollections.observableArrayList(SURFACE_COLORS.keySet()));
        backgroundChoice.setValue("Slate");
        backgroundChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            Color selected = SURFACE_COLORS.getOrDefault(newVal, SURFACE_COLORS.get("Slate"));
            setBackground(new Background(new BackgroundFill(selected, CornerRadii.EMPTY, Insets.EMPTY)));
        });

        ChoiceBox<String> roundingChoice = new ChoiceBox<>(FXCollections.observableArrayList("Soft", "Rounded", "Sharp"));
        roundingChoice.setValue("Rounded");

        alternateStatus.setWrapText(true);

        alternateSettingsPane.getChildren().setAll(
            new Label("Alternate View Settings"),
            new Separator(),
            new Label("Background"),
            backgroundChoice,
            new Label("Card Corners"),
            roundingChoice,
            new Label("Status"),
            alternateStatus,
            new Separator(),
            new Label("Custom Fields / Localization / Expenses / Bank placeholders added in alternate templates."));
        alternateSettingsPane.setSpacing(8);
        alternateSettingsPane.setPadding(new Insets(12));
        alternateSettingsPane.setStyle("-fx-background-color: #f7f8fe; -fx-background-radius: 14;");
        return alternateSettingsPane;
    }



    private VBox buildDatabaseSelectorPane()
    {
        TextField dbPath = new TextField();
        dbPath.setPromptText("/path/to/file.db");
        Button browse = new Button("Browse...");
        Button open = new Button("Open Database");
        Label state = new Label("No file selected.");
        browse.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select database file");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database files", "*.db", "*.mv.db", "*.h2.db"));
            File selected = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
            if (selected != null)
            {
                dbPath.setText(selected.getAbsolutePath());
                state.setText("Selected: " + selected.getName());
            }
        });
        open.setOnAction(e -> {
            String value = dbPath.getText();
            if (value == null || value.isBlank())
            {
                alternateStatus.setText("No database selected.");
                return;
            }
            alternateStatus.setText("Database selected:\n" + value);
        });
        databaseSelectorPane.getChildren().setAll(new Label("Open Database (.db)"), new HBox(8, dbPath, browse), open, state);
        databaseSelectorPane.setPadding(new Insets(12));
        databaseSelectorPane.setSpacing(10);
        return databaseSelectorPane;
    }

    private VBox buildCompanySelectorPane()
    {
        ComboBox<String> companies = new ComboBox<>(FXCollections.observableArrayList(loadAvailableCompanies()));
        companies.setPromptText("Select company");
        ListView<String> recent = new ListView<>(FXCollections.observableArrayList(loadAvailableCompanies()));
        recent.setPrefHeight(140);
        Button open = new Button("Open Company");
        open.setOnAction(e -> {
            String selected = companies.getValue();
            if (selected == null || selected.isBlank())
            {
                alternateStatus.setText("No company selected.");
                return;
            }
            alternateStatus.setText("Company selected:\n" + selected);
        });
        companySelectorPane.getChildren().setAll(new Label("Open Company"), companies, new Label("Recent Companies"), recent, open);
        companySelectorPane.setPadding(new Insets(12));
        companySelectorPane.setSpacing(10);
        return companySelectorPane;
    }

    private List<String> loadAvailableCompanies()
    {
        Path root = Paths.get(".").toAbsolutePath().normalize();
        int maxDepth = Integer.getInteger("npbk.company.scan.depth", 3);
        try (var stream = Files.find(root, maxDepth, (path, attrs) -> attrs.isRegularFile() && isDatabaseLike(path)))
        {
            List<String> matches = stream
                .map(root::relativize)
                .map(Path::toString)
                .sorted()
                .collect(Collectors.toList());
            if (!matches.isEmpty())
            {
                return matches;
            }
        }
        catch (Exception ignored)
        {
            // fallback below
        }
        return List.of("Purine Inc.", "John's Inc.", "San Crescent Accounting");
    }

    private boolean isDatabaseLike(Path path)
    {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".db") || name.endsWith(".mv.db") || name.endsWith(".h2.db");
    }

    private void openDatabaseSelector()
    {
        dashboardCanvas.setVisible(false);
        dashboardCanvas.setManaged(false);
        alternateSettingsPane.setVisible(false);
        alternateSettingsPane.setManaged(false);
        panelHost.setVisible(false);
        panelHost.setManaged(false);
        alternateContentPane.setVisible(true);
        alternateContentPane.setManaged(true);
        alternateContentPane.getChildren().setAll(buildDatabaseSelectorPane());
    }

    private void openCompanySelector()
    {
        dashboardCanvas.setVisible(false);
        dashboardCanvas.setManaged(false);
        alternateSettingsPane.setVisible(false);
        alternateSettingsPane.setManaged(false);
        panelHost.setVisible(false);
        panelHost.setManaged(false);
        alternateContentPane.setVisible(true);
        alternateContentPane.setManaged(true);
        alternateContentPane.getChildren().setAll(buildCompanySelectorPane());
    }

    private void openPanel(AppPanelId id)
    {
        WorkspaceRouteDecision decision = workspaceRouter.decide(id);

        boolean dashboard = decision.isDashboard();
        boolean alternateCustomPane = decision.isAlternateCustomPane();
        boolean panelHostBackedPanel = decision.isPanelHost();

        dashboardCanvas.setVisible(dashboard);
        dashboardCanvas.setManaged(dashboard);
        alternateSettingsPane.setVisible(id == AppPanelId.SETTINGS);
        alternateSettingsPane.setManaged(id == AppPanelId.SETTINGS);
        alternateContentPane.setVisible(alternateCustomPane && id != AppPanelId.SETTINGS);
        alternateContentPane.setManaged(alternateCustomPane && id != AppPanelId.SETTINGS);
        panelHost.setVisible(panelHostBackedPanel);
        panelHost.setManaged(panelHostBackedPanel);

        if (id == AppPanelId.SETTINGS)
        {
            buildAlternateSettingsPane();
        }
        else if (alternateCustomPane)
        {
            Node template = switch (id)
            {
                case CHART_OF_ACCOUNTS -> AlternateChartOfAccountsView.build();
                case LEDGER_REGISTER -> AlternateManualJournalView.build();
                case INVENTORY -> AlternateInventoryTransferOrdersView.build();
                case REPORTS_WORKSPACE -> AlternateReportsOverviewView.build();
                default -> new Label("Template pending");
            };
            alternateContentPane.getChildren().setAll(template);
        }
        else if (panelHostBackedPanel)
        {
            panelHost.show(id);
        }
        nav.highlight(id);
    }

    private void openInspectorForSelection(String title, String body)
    {
        alternateStatus.setText(title + "\n" + body);
    }

    private void openRecordServicePanel(nonprofitbookkeeping.ui.RecordServicePanelRegistry.PanelBinding binding)
    {
        if (binding.workspacePanelId() != null)
        {
            openPanel(binding.workspacePanelId());
            return;
        }
        AppPanel panel = binding.panelFactory().get();
        openInspectorForSelection(binding.displayName(), panel.title() + " opened in alternate shell.");
    }
}
