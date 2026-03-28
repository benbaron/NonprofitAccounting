package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.ui.ThemeManager;
import nonprofitbookkeeping.ui.panels.SclxImportPanelFX;

/**
 * Opens the SCLX import workspace panel.
 */
public class ImportSclxActionFX implements EventHandler<ActionEvent>
{
    private final Stage owner;

    public ImportSclxActionFX(Stage owner)
    {
        this.owner = owner;
    }

    @Override
    public void handle(ActionEvent event)
    {
        Stage stage = new Stage();
        stage.initOwner(this.owner);
        stage.setTitle("Import SCLX");
        BorderPane root = new BorderPane(new SclxImportPanelFX(this.owner));
        Scene scene = new Scene(root, 860, 620);
        ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.show();
    }
}
