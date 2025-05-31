package nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.Company;
import javafx.scene.layout.VBox; // Using VBox as a basic layout pane

/**
 * A JavaFX panel component responsible for allowing users to select,
 * create, or open a company data file.
 * It uses a callback mechanism ({@link OnCompanyOpenedHandler}) to notify
 * other parts of the application when a company has been successfully opened or processed.
 */
public class CompanySelectionPanelFX extends VBox {

    /**
     * Functional interface for a callback to be invoked when a company
     * has been successfully opened or processed by this panel.
     */
    @FunctionalInterface
    public interface OnCompanyOpenedHandler {
        /**
         * Called when a company's data has been successfully loaded/processed.
         *
         * @param company The {@link Company} object that was opened or processed.
         */
        void onCompanyOpened(Company company);
    }

    private final OnCompanyOpenedHandler companyOpenedHandler;

    /**
     * Constructs a new CompanySelectionPanelFX.
     *
     * @param openedHandler The handler to be called when a company is successfully opened
     *                      or processed. Must not be null.
     * @throws IllegalArgumentException if openedHandler is null.
     */
    public CompanySelectionPanelFX(OnCompanyOpenedHandler openedHandler) {
        if (openedHandler == null) {
            throw new IllegalArgumentException("OnCompanyOpenedHandler cannot be null.");
        }
        this.companyOpenedHandler = openedHandler;

        // Basic panel setup (further UI elements would be added here)
        this.setSpacing(10);
        // Example:
        // javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Select or Create Company");
        // getChildren().add(titleLabel);
        // TODO: Add UI elements for company selection, creation, opening.
        // These elements would eventually use this.companyOpenedHandler.onCompanyOpened(company);
    }

    // Placeholder for a method that might be called when a "Open Company" button is clicked
    // This would involve FileChooser, loading logic, and then:
    // if (companySuccessfullyLoaded) {
    //    Company loadedCompany = ...; // get the loaded company object
    //    this.companyOpenedHandler.onCompanyOpened(loadedCompany);
    // }
}
