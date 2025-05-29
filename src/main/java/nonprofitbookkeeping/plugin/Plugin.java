package nonprofitbookkeeping.plugin;

import nonprofitbookkeeping.core.ApplicationContext;
import javafx.scene.control.MenuBar;

public interface Plugin {
    String getName();
    String getDescription();
    void initialize(ApplicationContext context) throws Exception;
    void addMenuItems(MenuBar mainMenuBar);
    void shutdown();
}
