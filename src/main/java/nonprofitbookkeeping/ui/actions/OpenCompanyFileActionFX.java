
package nonprofitbookkeeping.ui.actions;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import nonprofitbookkeeping.ui.panels.CompanySelectionPanelFX;

// TODO: Auto-generated Javadoc
/**
 * Presents a dialog that lists companies stored inside the application database and allows
 * the user to open one. Once a company is selected it is loaded into the {@link CurrentCompany}
 * context and the optional callback is invoked.
 */
public class OpenCompanyFileActionFX
{
        
        /** The on success callback. */
        private final Runnable onSuccessCallback;

        /**
         * Constructor
         * 
         * Constructs and displays the dialog that lists available companies.
         *
         * @param owner the owner
         * @param onSuccessCallback the on success callback
         */
        public OpenCompanyFileActionFX(Stage owner,
                                       Runnable onSuccessCallback)
        {
                this.onSuccessCallback = onSuccessCallback;
                Stage dialog = new Stage();
                dialog.initOwner(owner);
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.setTitle("Open Company");

                CompanySelectionPanelFX panel = new CompanySelectionPanelFX();
                panel.setOnCompanyOpenedHandler(company -> {
                        dialog.close();

                        if (this.onSuccessCallback != null)
                        {
                                this.onSuccessCallback.run();
                        }
                });

                panel.setOnError(message -> AlertBox.showError(owner, message));

                dialog.setScene(new Scene(panel, 700, 500));
                dialog.show();
        }

}
