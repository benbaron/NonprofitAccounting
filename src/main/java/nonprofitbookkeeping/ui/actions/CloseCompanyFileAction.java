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
 * Action class responsible for handling the closing of the current company file.
 * This action first attempts to persist (save) the current company's data
 * and then formally closes the company within the application's context
 * (e.g., by updating {@link CurrentCompany} state).
 */
public class CloseCompanyFileAction
{
	/** Flag indicating whether the close operation was completed. */
	private boolean closed;
	
	/**
	 * Constructs and executes the action to close the current company file.
	 * The dialog presented offers the user three choices:
	 * <ul>
	 *   <li><strong>Yes</strong> &ndash; save the company and close it.</li>
	 *   <li><strong>No</strong> &ndash; close without saving.</li>
	 *   <li><strong>Cancel</strong> &ndash; abort the close operation entirely.</li>
	 * </ul>
	 *
	 * @param primaryStage The owner stage for the confirmation dialog.
	 */
	public CloseCompanyFileAction(Stage primaryStage)
	{
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
			"Save company before closing?",
			ButtonType.YES, 
			ButtonType.NO, 
			ButtonType.CANCEL);
		confirm.initOwner(primaryStage);
		Optional<ButtonType> res = confirm.showAndWait();
		
		if (res.isEmpty() || res.get() == ButtonType.CANCEL)
		{
			this.closed = false;
			return; // user cancelled the action
		}
		
		if (res.get() == ButtonType.YES)
		{
			
			try
			{
				CurrentCompany.persist();
			}
			catch (IOException | ActionCancelledException | NoFileCreatedException e)
			{
				e.printStackTrace();
			}
			
		}
		
		CurrentCompany.close();
		this.closed = true;
	}
	
	/**
	 * @return {@code true} if the company was closed as a result of this action
	 */
	public boolean isClosed()
	{
		return this.closed;
	}
}
