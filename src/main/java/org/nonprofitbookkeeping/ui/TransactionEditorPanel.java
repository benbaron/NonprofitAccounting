package org.nonprofitbookkeeping.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
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
    private final Runnable onClose;
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private boolean hydrating = false;

    public TransactionEditorPanel()
    {
        this(null, null);
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

        save.setOnAction(e -> onSave());
        post.setOnAction(e -> validateOrPost());
        journal.setOnAction(e -> showJournal());

        hydrating = true;
        loadFromDraft(row);
        if (row == null)
        {
            resetToBlankDraft();
        }

        installDirtyTracking();
        hydrating = false;
    }

    private Node buildHeaderForm()
    {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(8);
        g.setPadding(new Insets(8, 0, 8, 0));

        installSelectAllOnDoubleClick(date);
        installSelectAllOnDoubleClick(payee);
        installSelectAllOnDoubleClick(memo);
        installSelectAllOnDoubleClick(bank);

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
                if (!row.isEmpty() && e.getClickCount() == 2 &&
                    e.getButton() == javafx.scene.input.MouseButton.PRIMARY)
                {
                    splitTable.getSelectionModel().select(row.getIndex());
                    splitTable.edit(row.getIndex(), splitTable.getColumns().get(0));
                }
            });
            return row;
        });
    }

    private Node buildSplitEditor()
    {
        Label lbl = new Label("Splits");
        lbl.getStyleClass().add("subheader");

        Button addLine = new Button("+ Add Line");
        Button removeLine = new Button("– Remove");
        ToolBar tb = new ToolBar(addLine, removeLine);

        addLine.setOnAction(e -> {
            splitTable.getItems().add(new SplitRow("", "", "0.00", "", "", "", ""));
            markDirty();
        });
        removeLine.setOnAction(e -> {
            SplitRow sel = splitTable.getSelectionModel().getSelectedItem();
            if (sel != null)
            {
                splitTable.getItems().remove(sel);
                markDirty();
            }
        });

        VBox box = new VBox(6, lbl, tb, splitTable);
        VBox.setVgrow(splitTable, Priority.ALWAYS);
        return box;
    }

    private TableColumn<SplitRow, String> col(String name,
                                               java.util.function.Function<SplitRow, SimpleStringProperty> getter)
    {
        TableColumn<SplitRow, String> c = new TableColumn<>(name);
        c.setCellValueFactory(v -> getter.apply(v.getValue()));
        c.setCellFactory(TextFieldTableCell.forTableColumn());
        c.setOnEditCommit(e -> markDirty());
        return c;
    }

    private void validateOrPost()
    {
        ValidationResult validation = validateAmounts();
        if (!validation.valid())
        {
            status.setText("Validation failed: " + validation.message());
            return;
        }

        status.setText(String.format("Validated draft. Split total: %.2f", validation.total()));
    }

    private ValidationResult validateAmounts()
    {
        double total = 0.0;
        for (int i = 0; i < splitTable.getItems().size(); i++)
        {
            String raw = splitTable.getItems().get(i).amountProperty().get();
            String normalized = raw == null || raw.isBlank() ? "0" : raw.trim();
            try
            {
                total += Double.parseDouble(normalized);
            }
            catch (NumberFormatException ex)
            {
                return new ValidationResult(false, 0,
                    "Row " + (i + 1) + " amount is not numeric ('" + normalized + "')");
            }
        }
        return new ValidationResult(true, total, "OK");
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

    private void installDirtyTracking()
    {
        date.textProperty().addListener((obs, oldV, newV) -> markDirty());
        payee.textProperty().addListener((obs, oldV, newV) -> markDirty());
        memo.textProperty().addListener((obs, oldV, newV) -> markDirty());
        bank.textProperty().addListener((obs, oldV, newV) -> markDirty());

        splitTable.getItems().forEach(this::bindRowDirtyTracking);
        splitTable.getItems().addListener((ListChangeListener<SplitRow>) change -> {
            while (change.next())
            {
                if (change.wasAdded())
                {
                    change.getAddedSubList().forEach(this::bindRowDirtyTracking);
                }
            }
            markDirty();
        });

    }

    private void bindRowDirtyTracking(SplitRow row)
    {
        row.accountProperty().addListener((obs, oldV, newV) -> markDirty());
        row.fundProperty().addListener((obs, oldV, newV) -> markDirty());
        row.amountProperty().addListener((obs, oldV, newV) -> markDirty());
        row.activityProperty().addListener((obs, oldV, newV) -> markDirty());
        row.merchantProperty().addListener((obs, oldV, newV) -> markDirty());
        row.nmrProperty().addListener((obs, oldV, newV) -> markDirty());
        row.notesProperty().addListener((obs, oldV, newV) -> markDirty());
    }

    private void markDirty()
    {
        if (!hydrating)
        {
            dirty.set(true);
        }
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

    Label statusLabelForTest()
    {
        return status;
    }

    TableView<SplitRow> splitTableForTest()
    {
        return splitTable;
    }

    boolean isDirtyForTest()
    {
        return dirty.get();
    }

    @Override
    public void onSave()
    {
        ValidationResult validation = validateAmounts();
        if (!validation.valid())
        {
            status.setText("Cannot save: " + validation.message());
            return;
        }

        status.setText(
            "Saved transaction draft for " + (payee.getText().isBlank() ? "(unnamed payee)" : payee.getText()));
        if (onClose != null)
        {
            onClose.run();
        }
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
        ButtonType closeButtonType = ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        dialog.getDialogPane().setMinSize(980, 680);
        dialog.getDialogPane().setPrefSize(1200, 840);
        dialog.setResizable(true);

        Node closeButton = dialog.getDialogPane().lookupButton(closeButtonType);
        closeButton.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (!confirmClose(dialog.getDialogPane().getScene() == null ? owner : dialog.getDialogPane().getScene().getWindow()))
            {
                evt.consume();
            }
        });

        dialog.setOnShown(e -> {
            if (dialog.getDialogPane().getScene() != null &&
                dialog.getDialogPane().getScene().getWindow() instanceof Stage stage)
            {
                stage.setMaximized(true);
                stage.setOnCloseRequest(we -> {
                    if (!confirmClose(stage))
                    {
                        we.consume();
                    }
                });
            }
        });
        dialog.showAndWait();
    }

    private boolean confirmClose(Window owner)
    {
        if (!dirty.get())
        {
            return true;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        if (owner != null)
        {
            confirm.initOwner(owner);
        }
        confirm.setHeaderText("Discard unsaved changes?");
        confirm.setContentText("You have unsaved transaction edits. Close without saving?");

        ButtonType discard = new ButtonType("Discard", ButtonBar.ButtonData.YES);
        ButtonType keepEditing = new ButtonType("Keep Editing", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(discard, keepEditing);
        return confirm.showAndWait().orElse(keepEditing) == discard;
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

        public SimpleStringProperty accountProperty() { return account; }
        public SimpleStringProperty fundProperty() { return fund; }
        public SimpleStringProperty amountProperty() { return amount; }
        public SimpleStringProperty activityProperty() { return activity; }
        public SimpleStringProperty merchantProperty() { return merchant; }
        public SimpleStringProperty nmrProperty() { return nmr; }
        public SimpleStringProperty notesProperty() { return notes; }
    }

    private record ValidationResult(boolean valid, double total, String message) {}
}
