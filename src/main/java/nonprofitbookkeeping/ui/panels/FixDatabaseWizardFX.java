package nonprofitbookkeeping.ui.panels;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.persistence.entity.CompanyEntity;

/**
 * Wizard panel that lists all company rows in the database and allows the user
 * to delete invalid rows and choose one to open.
 */
public class FixDatabaseWizardFX extends BorderPane {
    private final List<RowEntry> entries = new ArrayList<>();

    private FixDatabaseWizardFX(List<CompanyEntity> entities) {
        ObjectMapper mapper = new ObjectMapper();
        ToggleGroup openGroup = new ToggleGroup();
        VBox listBox = new VBox(5);
        listBox.setPadding(new Insets(10));

        for (CompanyEntity ce : entities) {
            boolean valid = true;
            String status = "Valid";
            if (ce.getName() == null || ce.getName().isBlank()) {
                valid = false;
                status = "Missing name";
            }
            if (ce.getJsonData() == null || ce.getJsonData().isBlank()) {
                if (status.equals("Valid")) {
                    status = "Missing JSON";
                } else {
                    status += ", Missing JSON";
                }
            } else {
                try {
                    mapper.readValue(ce.getJsonData(), Company.class);
                } catch (Exception e) {
                    valid = false;
                    status = "Invalid JSON";
                }
            }
            CheckBox delete = new CheckBox();
            RadioButton open = new RadioButton();
            open.setToggleGroup(openGroup);
            open.setDisable(!valid);
            Label label = new Label("ID " + ce.getId() + " - " + (ce.getName() == null ? "<unnamed>" : ce.getName()) + " (" + status + ")");
            HBox row = new HBox(10, delete, open, label);
            row.setAlignment(Pos.CENTER_LEFT);
            entries.add(new RowEntry(ce.getId(), delete, open, valid));
            listBox.getChildren().add(row);
        }

        ScrollPane scroller = new ScrollPane(listBox);
        scroller.setFitToWidth(true);
        setCenter(scroller);

        Button ok = new Button("OK");
        Button cancel = new Button("Cancel");
        HBox buttons = new HBox(10, ok, cancel);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10));
        setBottom(buttons);

        ok.setOnAction(e -> ((Stage) getScene().getWindow()).close());
        cancel.setOnAction(e -> {
            entries.clear();
            ((Stage) getScene().getWindow()).close();
        });
    }

    private List<Long> getDeleteIds() {
        return entries.stream().filter(r -> r.delete.isSelected()).map(r -> r.id).collect(Collectors.toList());
    }

    private Long getOpenId() {
        return entries.stream().filter(r -> r.open.isSelected()).map(r -> r.id).findFirst().orElse(null);
    }

    /**
     * Show the wizard and return the user's selections.
     */
    public static Result show(Stage owner, List<CompanyEntity> entities) {
        FixDatabaseWizardFX pane = new FixDatabaseWizardFX(entities);
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Fix Database");
        dialog.setScene(new Scene(pane, 600, 400));
        dialog.showAndWait();
        if (pane.entries.isEmpty()) {
            return new Result(true, List.of(), null);
        }
        return new Result(false, pane.getDeleteIds(), pane.getOpenId());
    }

    /** Result from the wizard. */
    public static class Result {
        public final boolean cancelled;
        public final List<Long> deleteIds;
        public final Long openId;

        public Result(boolean cancelled, List<Long> deleteIds, Long openId) {
            this.cancelled = cancelled;
            this.deleteIds = deleteIds;
            this.openId = openId;
        }
    }

    private static class RowEntry {
        final long id;
        final CheckBox delete;
        final RadioButton open;
        final boolean valid;

        RowEntry(long id, CheckBox delete, RadioButton open, boolean valid) {
            this.id = id;
            this.delete = delete;
            this.open = open;
            this.valid = valid;
        }
    }
}

