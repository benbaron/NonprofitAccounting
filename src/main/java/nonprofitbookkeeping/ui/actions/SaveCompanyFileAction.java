/**
 * nonprofit-scaledger-ribbon.zip_expanded StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */

package nonprofitbookkeeping.ui.actions;

import java.io.IOException;

import javafx.stage.Stage;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * Action class responsible for explicitly flushing the current company to
 * persistent storage without displaying a confirmation dialog.
 *
 * <p>The active H2 database is normally synchronized as changes are made. This
 * action remains available for compatibility and explicit flush requests, but
 * it must also be safe to invoke from application shutdown without blocking on
 * user interaction.</p>
 */
public class SaveCompanyFileAction
{
    /**
     * Constructs and executes the save action.
     *
     * @param primaryStage retained for source compatibility with existing callers
     */
    public SaveCompanyFileAction(Stage primaryStage)
    {
        if (!CurrentCompany.isOpen())
        {
            return;
        }

        try
        {
            CurrentCompany.persist();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
