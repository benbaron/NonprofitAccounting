package org.nonprofitbookkeeping.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Window;

/**
 * Represents the TransactionEditorPanel component in the nonprofit bookkeeping application.
 */
public class TransactionEditorPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final TableView<SplitRow> splitTable = new TableView<>();

    private final TextField date = new TextField();
    private final TextField payee = new TextField();
    private final TextField memo = new TextField();
    private final TextField bank = new TextField();
    private final Label status = new Label("Ready");

    private final Button save = new Button("Save");
    private boolean dirty;
    private final Runnable onClose;

    public TransactionEditorPanel()
    {
        this(TransactionDraftContext.getSelectedRow(), null);
    }

    public TransactionEditorPanel(LedgerRegisterPanel.Row row, Runnable onClose)
    {
        this.onClose = onClose;
        root.setPadding(new Insets(8));

        Label title = new Label("Transaction Editor");
        title.getStyleClass().add("panel-title");

        Button post = new Button("Post / Validate");
        Button journal = new Button("Journal View");
        HBox actions = new HBox(8, save, post, journal);

        VBox top = new VBox(6, title, actions, new Separator(), buildHeaderForm());
        root.setTop(top);

        buildSplitTable();
        root.setCenter(buildSplitEditor());
        root.setBottom(new VBox(new Separator(), status));

        installDirtyTracking();

        save.setOnAction(e -> onSave());
        post.setOnAction(e -> validateOrPost());
        journal.setOnAction(e -> showJournal());

        loadFromDraft(row);
        if (row == null)
        {
            resetToBlankDraft();
        }
    }

    private Node buildHeaderForm()
    {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(8);
        g.setPadding(new Insets(8, 0, 8, 0));

        date.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
            {
                date.selectAll();
            }
        });
        payee.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
            {
                payee.selectAll();
            }
        });
        memo.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
            {
                memo.selectAll();
            }
        });
        bank.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
            {
                bank.selectAll();
            }
        });

        int r = 0;
        g.add(new Label("Date"), 0, r);
        g.add(date, 1, r);
        g.add(new Label("Payee"), 2, r);
        g.add(payee, 3, r);
        r++;
        g.add(new Label("Memo"), 0, r);
        g.add(memo, 1, r, 3, 1);
        r++;
        g.add(new Label("Bank"), 0, r);
        g.add(bank, 1, r);

        g.getColumnConstraints().addAll(new ColumnConstraints(70), new ColumnConstraints(220),
            new ColumnConstraints(70), new ColumnConstraints(220));
        g.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().get(3).setHgrow(Priority.ALWAYS);

        return g;
    }

    private void installSelectAllOnDoubleClick(TextField field)
    {
        field.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
            {
                field.selectAll();
            }
        });
    }

    private void buildSplitTable()
    {
        splitTable.setEditable(true);
        splitTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        splitTable.getColumns().add(col("Account", SplitRow::accountProperty));
        splitTable.getColumns().add(col("Fund", SplitRow::fundProperty));
        splitTable.getColumns().add(col("Amount", SplitRow::amountProperty));
        splitTable.getColumns().add(col("Activity", SplitRow::activityProperty));
        splitTable.getColumns().add(col("Merchant", SplitRow::merchantProperty));
        splitTable.getColumns().add(col("NMR", SplitRow::nmrProperty));
        splitTable.getColumns().add(col("Notes", SplitRow::notesProperty));

        splitTable.setItems(FXCollections.observableArrayList(
            new SplitRow("", "", "", "", "", "", ""),
            new SplitRow("", "", "", "", "", "", "")));
        splitTable.setRowFactory(tv -> {
            TableRow<SplitRow> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getClickCount() == 2 && e.getButton() == javafx.scene.input.MouseButton.PRIMARY)
                {
                    splitTable.getSelectionModel().select(row.getIndex());
                    splitTable.edit(row.getIndex(), splitTable.getColumns().get(0));
                }
            });
            return row;
        });
    }

    private void installDirtyTracking()
    {
        date.textProperty().addListener((obs, oldVal, newVal) -> markDirty());
        payee.textProperty().addListener((obs, oldVal, newVal) -> markDirty());
        memo.textProperty().addListener((obs, oldVal, newVal) -> markDirty());
        bank.textProperty().addListener((obs, oldVal, newVal) -> markDirty());
        splitTable.getItems().forEach(this::trackSplitRow);
        splitTable.getItems().addListener((javafx.collections.ListChangeListener<SplitRow>) c -> {
            markDirty();
            while (c.next())
            {
                if (c.wasAdded())
                {
                    c.getAddedSubList().forEach(this::trackSplitRow);
                }
            }
        });
    }

    private void trackSplitRow(SplitRow row)
    {
        row.accountProperty().addListener((obs, oldVal, newVal) -> markDirty());
        row.fundProperty().addListener((obs, oldVal, newVal) -> markDirty());
        row.amountProperty().addListener((obs, oldVal, newVal) -> markDirty());
        row.activityProperty().addListener((obs, oldVal, newVal) -> markDirty());
        row.merchantProperty().addListener((obs, oldVal, newVal) -> markDirty());
        row.nmrProperty().addListener((obs, oldVal, newVal) -> markDirty());
        row.notesProperty().addListener((obs, oldVal, newVal) -> markDirty());
    }

    private void markDirty()
    {
        dirty = true;
    }

    private Node buildSplitEditor()
    {
        Label lbl = new Label("Splits");
        lbl.getStyleClass().add("subheader");

        Button addLine = new Button("+ Add Line");
        Button removeLine = new Button("– Remove");
        ToolBar tb = new ToolBar(addLine, removeLine);

        addLine.setOnAction(e -> splitTable.getItems().add(new SplitRow("", "", "0.00", "", "", "", "")));
        removeLine.setOnAction(e -> {
            SplitRow sel = splitTable.getSelectionModel().getSelectedItem();
            if (sel != null)
            {
                splitTable.getItems().remove(sel);
            }
        });

        VBox box = new VBox(6, lbl, tb, splitTable);
        VBox.setVgrow(splitTable, Priority.ALWAYS);
        return box;
    }

    private TableColumn<SplitRow, String> col(String name,
                                               java.util.function.Function<SplitRow, SimpleStringProperty> prop)
    {
        TableColumn<SplitRow, String> c = new TableColumn<>(name);
        c.setCellValueFactory(v -> prop.apply(v.getValue()));
        c.setCellFactory(TextFieldTableCell.forTableColumn());
        return c;
    }

    private void validateOrPost()
    {
        double total = splitTable.getItems().stream().map(SplitRow::amountProperty).map(SimpleStringProperty::get)
            .mapToDouble(v -> {
                try
                {
                    return Double.parseDouble(v == null || v.isBlank() ? "0" : v);
                }
                catch (NumberFormatException ex)
                {
                    return 0.0;
                }
            }).sum();

        status.setText(String.format("Validated draft. Split total: %.2f", total));
    }

    private void showJournal()
    {
        String text = splitTable.getItems().stream()
            .map(r -> String.format("%s | %s | %s", r.accountProperty().get(), r.fundProperty().get(),
                r.amountProperty().get()))
            .reduce((a, b) -> a + "\n" + b)
            .orElse("(No lines)");
        Alert a = new Alert(Alert.AlertType.INFORMATION, text);
        a.setHeaderText("Journal Preview");
        a.showAndWait();
    }

    private void loadFromDraft(LedgerRegisterPanel.Row row)
    {
        if (row == null)
        {
            return;
        }
        date.setText(row.date());
        payee.setText(row.payee());
        memo.setText(row.memo());
        bank.setText(row.bank());
        status.setText("Loaded from Ledger Register selection");

        ObservableList<SplitRow> rows = splitTable.getItems();
        rows.clear();
        rows.add(new SplitRow("Cash/Bank", "General", "0.00", "", row.payee(), "", row.memo()));
        rows.add(new SplitRow("Expense", "General", "0.00", "", row.payee(), "", "Balancing line"));
    }

    private void resetToBlankDraft()
    {
        date.setText("");
        payee.setText("");
        memo.setText("");
        bank.setText("");
        status.setText("New transaction draft");
    }

    @Override
    public String title()
    {
        return "Transaction Editor";
    }

    @Override
    public Node root()
    {
        return root;
    }

    @Override
    public void onSave()
    {
        for (SplitRow row : splitTable.getItems())
        {
            if (!isValidAmount(row.amountProperty().get()))
            {
                status.setText("Cannot save: invalid amount '" + row.amountProperty().get() + "'.");
                return;
            }
        }
        status.setText(
            "Saved transaction draft for " + (payee.getText().isBlank() ? "(unnamed payee)" : payee.getText()));
        dirty = false;
        if (onClose != null)
        {
            onClose.run();
        }
    }

    private boolean isValidAmount(String amount)
    {
        if (amount == null || amount.isBlank())
        {
            return true;
        }
        try
        {
            Double.parseDouble(amount);
            return true;
        }
        catch (NumberFormatException ex)
        {
            return false;
        }
    }

    boolean isDirtyForTest()
    {
        return dirty;
    }

    TableView<SplitRow> splitTableForTest()
    {
        return splitTable;
    }

    Label statusLabelForTest()
    {
        return status;
    }

    public void showAsDialog(Window owner, String title)
    {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        if (owner != null)
        {
            dialog.initOwner(owner);
        }
        dialog.getDialogPane().setContent(root());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setMinSize(980, 680);
        dialog.getDialogPane().setPrefSize(1200, 840);
        dialog.setResizable(true);
        dialog.setOnShown(e -> {
            if (dialog.getDialogPane().getScene() != null && dialog.getDialogPane().getScene().getWindow() instanceof javafx.stage.Stage stage)
            {
                stage.setMaximized(true);
            }
        });
        dialog.showAndWait();
    }

    public static final class SplitRow
    {
        private final SimpleStringProperty account;
        private final SimpleStringProperty fund;
        private final SimpleStringProperty amount;
        private final SimpleStringProperty activity;
        private final SimpleStringProperty merchant;
        private final SimpleStringProperty nmr;
        private final SimpleStringProperty notes;

        public SplitRow(String account, String fund, String amount, String activity, String merchant, String nmr,
                        String notes)
        {
            this.account = new SimpleStringProperty(account);
            this.fund = new SimpleStringProperty(fund);
            this.amount = new SimpleStringProperty(amount);
            this.activity = new SimpleStringProperty(activity);
            this.merchant = new SimpleStringProperty(merchant);
            this.nmr = new SimpleStringProperty(nmr);
            this.notes = new SimpleStringProperty(notes);
        }

        public SimpleStringProperty accountProperty()
        {
            return account;
        }

        public SimpleStringProperty fundProperty()
        {
            return fund;
        }

        public SimpleStringProperty amountProperty()
        {
            return amount;
        }

        public SimpleStringProperty activityProperty()
        {
            return activity;
        }

        public SimpleStringProperty merchantProperty()
        {
            return merchant;
        }

        public SimpleStringProperty nmrProperty()
        {
            return nmr;
        }

        public SimpleStringProperty notesProperty()
        {
            return notes;
        }
    }
}
