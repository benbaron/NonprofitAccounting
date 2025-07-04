/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * StoreCompanyFileAction.java
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
	/**
	 * Constructs and executes the action to close the current company file.
	 * The action involves two main steps:
	 * <ol>
	 *   <li>Attempting to persist the current company data using {@link CurrentCompany#persist()}.
	 *       Any exceptions during persistence ({@link IOException}, {@link ActionCancelledException},
	 *       {@link NoFileCreatedException}) are caught and their stack traces are printed to standard error.
	 *       The process continues regardless of persistence failure to ensure the company is closed.</li>
	 *   <li>Closing the company using {@link CurrentCompany#close()}, which typically updates
	 *       the application's state to reflect that no company is open.</li>
	 * </ol>
	 * 
	 * @param primaryStage The primary {@link Stage} of the JavaFX application. This parameter is
	 *                     currently not directly used in the constructor's logic but might be
	 *                     intended for displaying dialogs or interacting with the UI in a
	 *                     more complete implementation (e.g., confirming save on close).
	 */
        public CloseCompanyFileAction(Stage primaryStage)
        {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Save company before closing?");
                confirm.initOwner(primaryStage);
                Optional<ButtonType> res = confirm.showAndWait();

                if (res.isPresent() && res.get() == ButtonType.OK)
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
        }

	
}
