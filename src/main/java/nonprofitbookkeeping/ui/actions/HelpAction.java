
package nonprofitbookkeeping.ui.actions;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import nonprofitbookkeeping.ui.panels.HelpPanelFX;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the action of displaying help content in a JavaFX application.
 * This class implements {@link EventHandler} for {@link ActionEvent} to trigger
 * the display of help information using the {@link HelpPanelFX}. The panel itself
 * is resilient to missing help resources and will fall back to embedded guidance
 * text if the packaged HTML cannot be found, ensuring the action never leaves the
 * UI unresponsive.
 */
public class HelpAction implements EventHandler<ActionEvent>
{

        private static final Logger LOGGER = Logger.getLogger(HelpAction.class.getName());
	
	/** The owner Stage for the help window, used to ensure proper window modality and positioning. */
	private final Stage ownerStage;
	
	/**
	 * Constructs a new {@code HelpAction}.
	 *
	 * @param ownerStage The primary {@link Stage} of the JavaFX application, which will serve as
	 *                   the owner for the help window. This parameter must not be null.
	 * @throws IllegalArgumentException if {@code ownerStage} is null.
	 */
	public HelpAction(Stage ownerStage)
	{
		
		if (ownerStage == null)
		{
			throw new IllegalArgumentException("Owner stage cannot be null.");
		}
		
		this.ownerStage = ownerStage;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Handles the action event, typically triggered by selecting a "Help" menu item or button.
         * This method creates and displays a new {@link Stage} containing a {@link HelpPanelFX}.
         * The panel attempts to render the embedded HTML help contents but will gracefully
         * fall back to textual guidance if those resources are missing.
	 * </p>
	 *
	 * @param event The {@link ActionEvent} that triggered this handler (e.g., a menu item click).
	 */
        @Override public void handle(ActionEvent event)
        {
                try
                {
                        if (GraphicsEnvironment.isHeadless())
                        {
                                LOGGER.log(Level.WARNING, "JavaFX runtime not available; unable to show help window.");
                                return;
                        }

                        Runnable showHelp = () ->
                        {
                                try
                                {
                                        Stage helpStage = new Stage();
                                        helpStage.setTitle("Help");
                                        helpStage.initOwner(this.ownerStage);
                                        helpStage.initModality(Modality.WINDOW_MODAL);

                                        HelpPanelFX helpPanel = new HelpPanelFX(this.ownerStage);
                                        Scene scene = new Scene(helpPanel, 800, 600);
                                        helpStage.setScene(scene);
                                        helpStage.show();
                                }
                                catch (Throwable ex)
                                {
                                        LOGGER.log(Level.WARNING, "Unable to display help window", ex);
                                }
                        };

                        if (Platform.isFxApplicationThread())
                        {
                                showHelp.run();
                                return;
                        }

                        try
                        {
                                Platform.startup(showHelp);
                        }
                        catch (IllegalStateException alreadyStarted)
                        {
                                Platform.runLater(showHelp);
                        }
                }
                catch (RuntimeException unsupported)
                {
                        LOGGER.log(Level.WARNING, "JavaFX runtime not available; unable to show help window.", unsupported);
                }
        }

}
