/**
 * nonprofit-scaledger-ribbon.zip_expanded StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */

package nonprofitbookkeeping.ui.actions;

import java.io.IOException;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * Action class responsible for saving the current company's data to persistent storage.
 * This action does not close the company file after saving, allowing the user to
 * continue working with the currently open company.
 */
public class SaveCompanyFileAction
{
	
	/**
	 * Constructs and executes the action to save the current company's data.
	 * This constructor immediately attempts to persist the data associated with
	 * the {@link CurrentCompany} using the {@link CurrentCompany#persist()} method.
	 * <p>
	 * Any exceptions that occur during the persistence process (e.g., {@link IOException},
	 * {@link ActionCancelledException}, {@link NoFileCreatedException}) are caught,
	 * and their stack traces are printed to standard error. In a production application,
	 * these errors should ideally be communicated to the user via dialogs or other UI feedback.
	 * </p>
	 * 
	 * @param primaryStage The primary {@link Stage} of the JavaFX application. This parameter
	 *                     is currently not directly used within the constructor's logic but
	 *                     is accepted, potentially for future use in displaying UI feedback
	 *                     (e.g., success/error dialogs related to the save operation).
	 */
	public SaveCompanyFileAction(Stage primaryStage)
	{
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
			"Save current company file?");
		confirm.initOwner(primaryStage);
		Optional<ButtonType> res = confirm.showAndWait();
		
		if (res.isEmpty() || res.get() != ButtonType.OK)
		{
			return; // user chose not to save
		}
		
		try
		{
			CurrentCompany.persist();
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			e.printStackTrace();
		}
		
	}
	
	
}
