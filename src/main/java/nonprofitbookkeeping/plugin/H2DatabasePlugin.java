
package nonprofitbookkeeping.plugin;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.core.ApplicationContext;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.tools.JsonCompanyImporter;

import java.nio.file.Path;

public class H2DatabasePlugin implements Plugin {

    private ApplicationContext ctx;

    @Override public String getName() { return "H2 Database"; }
    @Override public String getDescription() { return "Adds H2 database support and JSON import"; }

    @Override public void initialize(ApplicationContext applicationContext) {
        this.ctx = applicationContext;
    }

    @Override public void addMenuItems(MenuBar mainMenuBar) {
        Menu db = new Menu("Database");
        MenuItem open = new MenuItem("Open/Create H2 DB...");
        MenuItem importJson = new MenuItem("Import JSON (zip) into DB...");
        db.getItems().addAll(open, importJson);

        open.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose H2 DB file (will create if missing)");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("H2 DB (*.mv.db or base name)", "*.*"));
            Stage stage = ctx.getPrimaryStage();
            var file = fc.showSaveDialog(stage);
            if (file == null) return;
            Path base = file.toPath();
            try {
                Database.init(base);
                Database.get().ensureSchema();
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Database initialized at: " + base);
                a.setHeaderText("H2 Ready"); a.showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Failed to open DB: " + ex.getMessage()).showAndWait();
            }
        });

        importJson.setOnAction(e -> {
            if (Database.get()==null) {
                new Alert(Alert.AlertType.WARNING, "Open/Create an H2 DB first.").showAndWait();
                return;
            }
            FileChooser fc = new FileChooser();
            fc.setTitle("Select legacy company JSON zip");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zip files", "*.zip"));
            Stage stage = ctx.getPrimaryStage();
            var file = fc.showOpenDialog(stage);
            if (file == null) return;
            try {
                JsonCompanyImporter.importZip(file.toPath());
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Imported company into DB.");
                a.setHeaderText("Import complete"); a.showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Import failed: " + ex.getMessage()).showAndWait();
            }
        });

        mainMenuBar.getMenus().add(db);
    }

    @Override public void shutdown() { }
}
