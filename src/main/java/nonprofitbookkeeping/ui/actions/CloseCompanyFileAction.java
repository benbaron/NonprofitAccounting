/**
 * nonprofit-scaledger-ribbon.zip_expanded StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */

package nonprofitbookkeeping.ui.actions;

import javafx.stage.Stage;
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
         * The current company is first flushed to the embedded database and then
         * marked as closed. No confirmation dialog is shown and no external
         * backup files are written.
         *
         * @param primaryStage Unused but retained for API compatibility.
         */
        public CloseCompanyFileAction(Stage primaryStage)
        {
                CurrentCompany.flushToDatabase();
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
