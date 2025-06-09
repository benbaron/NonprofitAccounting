package nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.Company;
import javafx.scene.layout.VBox; // Using VBox as a basic layout pane

/**
 * A JavaFX panel component responsible for allowing users to select,
 * create, or open a company data file.
 * It uses a callback mechanism ({@link OnCompanyOpenedHandler}) to notify
 * other parts of the application when a company has been successfully opened or processed.
 * This panel would typically include UI elements for browsing, selecting, or creating company files.
 */
public class CompanySelectionPanelFX extends VBox {

    /**
     * Functional interface for a callback to be invoked when a company
     * has been successfully opened, created, or otherwise processed by this panel.
     * The implementing class will handle the {@link Company} object (e.g., by updating the UI).
     */
    @FunctionalInterface
    public interface OnCompanyOpenedHandler {
        /**
         * Called when a company's data has been successfully loaded or a new company profile
         * has been processed and is ready for use.
         *
         * @param company The {@link Company} object that was opened, created, or processed.
         *                This object contains the company's data.
         */
        void onCompanyOpened(Company company);
    }

    /** Handler to be called when a company is opened or processed. */
    private final OnCompanyOpenedHandler companyOpenedHandler;

    /**
     * Constructs a new {@code CompanySelectionPanelFX}.
     * The panel is initialized with basic VBox spacing. UI elements for company
     * selection, creation, and opening need to be added to this panel.
     *
     * @param openedHandler The handler (callback) to be invoked when a company is successfully opened
     *                      or processed. This handler must not be null.
     * @throws IllegalArgumentException if {@code openedHandler} is null.
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
