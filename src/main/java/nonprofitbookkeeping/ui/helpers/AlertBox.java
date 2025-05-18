/**
 * nonprofit-scaledger-ribbon.zip_expanded AlertBox.java AlertBox
 */

package nonprofitbookkeeping.ui.helpers;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;

/**
 * Light wrapper around the JavaFX alert box
 */
public class AlertBox
{
	/** Pops an error dialog and waits for the user to dismiss it. */
	public static void showError(Window owner, String message)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(owner); // optional – ties it to your main stage
		alert.setTitle("Error");
		alert.setHeaderText("Something went wrong");
		alert.setContentText(message);
		
		// add extra buttons or graphics if you want
		// alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		
		alert.showAndWait(); // blocks until the user clicks OK
	}

	/**
	 * @param object
	 * @param string
	 */
	public static void showInfo(Window owner, String message)
	{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(owner); // optional – ties it to your main stage
		alert.setTitle("Information");
		alert.setHeaderText(message);
//		alert.setContentText(message);
		
		// add extra buttons or graphics if you want
		// alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
		
		alert.showAndWait(); // blocks until the user clicks OK
		
	}
	
}
