package nonprofitbookkeeping.ui.bootstrap;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Applies runtime stage/JVM decorations for the legacy JavaFX application.
 */
public class StageDecoratorService
{
    public void installShutdownHook(Runnable onShutdown)
    {
        Runtime.getRuntime().addShutdownHook(new Thread(onShutdown));
    }

    public void configureLoggingBridge()
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public void configureRuntimeProperties()
    {
        System.setProperty("net.sf.jasperreports.debug", "true");
        System.setProperty("net.sf.jasperreports.compile.class.debug", "true");
        System.setProperty("net.sf.jasperreports.compile.keep.java.file", "true");
        System.setProperty("net.sf.jasperreports.compiler.temp.dir", "C:/Users/benba/eclipse-workspace");
    }

    public void applyStageIcon(Stage stage, Class<?> resourceAnchor)
    {
        stage.getIcons().add(new Image(resourceAnchor.getResourceAsStream("../../cg-128px.png")));
    }
}
