/**
 * nonprofit-scaledger-ribbon.zip_expanded AlertBox.java AlertBox
 */

package nonprofitbookkeeping.ui.helpers;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Utility class providing static methods to display common JavaFX {@link Alert} dialogs.
 * This class simplifies showing error, information, and warning alerts by
 * pre-configuring {@link Alert} instances and handling their display.
 */
public class AlertBox
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AlertBox.class);

	private static void logPopup(AlertType type, String title, String header,
		String content)
	{
		if (type == null)
		{
			return;
		}
		String popupTitle = title == null ? "" : title;
		String popupHeader = header == null ? "" : header;
		String popupContent = content == null ? "" : content;

		switch (type)
		{
			case ERROR -> LOGGER.error(
				"Popup [{}] title='{}' header='{}' content='{}'",
				type,
				popupTitle,
				popupHeader,
				popupContent);
			case WARNING -> LOGGER.warn(
				"Popup [{}] title='{}' header='{}' content='{}'",
				type,
				popupTitle,
				popupHeader,
				popupContent);
			default -> LOGGER.info(
				"Popup [{}] title='{}' header='{}' content='{}'",
				type,
				popupTitle,
				popupHeader,
				popupContent);
		}
	}

	/**
	 * Displays an error dialog with a predefined title ("Error") and header text ("Something went wrong").
	 * The dialog is modal with respect to the owner window and waits for the user to dismiss it.
	 *
	 * @param owner The owner {@link Window} for this alert dialog. Can be null, in which case
	 *              the dialog is not owned by any window.
	 * @param message The main content message to be displayed in the error dialog.
	 */
	public static void showError(Window owner, String message)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(owner); 
		alert.setTitle("Error");
		alert.setHeaderText("Something went wrong"); // Standard header for errors
		alert.setContentText(message);
		logPopup(alert.getAlertType(), alert.getTitle(), alert.getHeaderText(),
			alert.getContentText());
		
		// Example for custom buttons (not used by default):
		// alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		
		alert.showAndWait(); // blocks until the user clicks OK (or another button if configured)
	}
	
	/**
	 * Displays an informational dialog with a predefined title ("Information").
	 * The provided message is used as the header text of the alert.
	 * The dialog is modal with respect to the owner window and waits for the user to dismiss it.
	 *
	 * @param owner The owner {@link Window} for this alert dialog. Can be null.
	 * @param message The message to be displayed as the header text of the information dialog.
	 */
	public static void showInfo(Window owner, String message)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(owner);
		alert.setTitle("Information");
		alert.setHeaderText(message); // Message used as header
		logPopup(alert.getAlertType(), alert.getTitle(), alert.getHeaderText(),
			alert.getContentText());
		// alert.setContentText(message); // Content text is not set, header is primary
		// display for message
		
		alert.showAndWait();
	}
	
	/**
	 * Displays a warning dialog with a predefined title ("Warning").
	 * The provided message is used as the header text of the alert.
	 * The dialog is modal with respect to the owner window and waits for the user to dismiss it.
	 *
	 * @param owner The owner {@link Window} for this alert dialog. Can be null.
	 * @param message The message to be displayed as the header text of the warning dialog.
	 */
	public static void showWarning(Window owner, String message)
	{
		Alert alert = new Alert(AlertType.WARNING);
		alert.initOwner(owner);
		alert.setTitle("Warning"); // Title changed to "Warning" for consistency
		alert.setHeaderText(message); // Message used as header
		logPopup(alert.getAlertType(), alert.getTitle(), alert.getHeaderText(),
			alert.getContentText());
		
		alert.showAndWait();
	}
	
	/**
	 * Show error.
	 *
	 * @param win the win
	 * @param header the header
	 * @param message the message
	 */
	public static void showError(Window win, String header, String message)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(win);
		alert.setTitle("Error");
		alert.setHeaderText(header); // Standard header for errors
		alert.setContentText(message);
		logPopup(alert.getAlertType(), alert.getTitle(), alert.getHeaderText(),
			alert.getContentText());
		
		// Example for custom buttons (not used by default):
		// alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		
		alert.showAndWait(); // blocks until the user clicks OK (or another button if configured)
	}
	
	
}
