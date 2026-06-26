/**
 * nonprofit-scaledger-ribbon.zip_expanded StoreCompanyFileAction.java
 * StoreCompanyFileAction
 */

package nonprofitbookkeeping.ui.actions;

import javafx.stage.Stage;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * Action class responsible for closing the current company.
 *
 * <p>The active H2 database is already kept synchronized as changes are made,
 * so closing a company does not prompt for or perform an additional save.</p>
 */
public class CloseCompanyFileAction
{
    /** Flag indicating whether the close operation was completed. */
    private boolean closed;

    /**
     * Constructs and executes the action to close the current company.
     *
     * @param primaryStage retained for source compatibility with existing callers
     */
    public CloseCompanyFileAction(Stage primaryStage)
    {
        CurrentCompany.close();
        this.closed = true;
    }

    /**
     * Checks whether the company was closed.
     *
     * @return {@code true} if the company was closed as a result of this action
     */
    public boolean isClosed()
    {
        return this.closed;
    }
}
