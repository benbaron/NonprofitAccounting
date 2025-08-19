
package nonprofitbookkeeping.ui.panels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.DatabaseService;
import nonprofitbookkeeping.ui.helpers.AlertBox;

/**
 * A JavaFX panel that allows users to select an existing company stored in the
 * database, preview basic information about it, open the selected company, or
 * initiate the creation of a new company. The panel lists companies by name and
 * shows basic details in a {@link TextArea} preview.
 */
public class CompanySelectionPanelFX extends BorderPane
{
	
	/**
	 * Inner class intended to handle callbacks when a company is opened.
	 * Note: This is currently a simple class with a stub method and is not a
	 * functional interface. For robust callback mechanisms in JavaFX,
	 * consider using a standard {@link javafx.event.EventHandler} or a custom
	 * {@code @FunctionalInterface}.
	 * @see nonprofitbookkeeping.ui.CompanySelectionPanelFX.OnCompanyOpenedHandler for an example of a functional interface.
	 */
	/**
	 * Callback interface invoked when a company is opened from this panel.
	 * Implementations can update UI state or trigger additional logic after
	 * the company has been loaded into {@link CurrentCompany}.
	 */
	@FunctionalInterface public interface OnCompanyOpenedHandler
	{
		/**
		 * Called when a company has been opened.
		 *
		 * @param company the company that was opened; may be {@code null}
		 *                 if loading failed
		 */
		void onCompanyOpened(Company company);
		
	}
	
        /** ListView component to display companies stored in the database. */
        private final ListView<Company> companyList = new ListView<>();
        /** ObservableList backing the {@code companyList}. */
        private final ObservableList<Company> companies = FXCollections.observableArrayList();
        /** TextArea used to display details of the selected company. */
        private final TextArea previewArea = new TextArea();

        /** Handler for when a company is successfully opened. */
        private OnCompanyOpenedHandler companyOpenedHandler;

        /** Database service for CRUD operations. */
        private final DatabaseService db = new DatabaseService();

        /**
         * Default constructor for {@code CompanySelectionPanelFX}.
         * Initializes the panel with default padding, constructs the UI elements
         * (company list, preview area, buttons), and populates the company list
         * by calling {@link #reloadCompanyList()}.
         */
        public CompanySelectionPanelFX()
        {
                setPadding(new Insets(10));
                buildUI();
                reloadCompanyList();
        }
	
	/**  
	 * Constructor CompanySelectionPanelFX
	 * @param object
	 */
	public CompanySelectionPanelFX(OnCompanyOpenedHandler companyOpenedHandler)
	{
		this.companyOpenedHandler = companyOpenedHandler;
	}
	
	/**
	 * Constructs and arranges the primary UI elements of this panel.
	 * This method sets up:
	 * <ul>
         *   <li>A {@link ListView} ({@code companyList}) on the left to display companies.</li>
         *   <li>A {@link TextArea} ({@code previewArea}) on the right for showing details of the selected company.</li>
	 *   <li>A {@link SplitPane} to manage the layout of the list and preview area.</li>
	 *   <li>"Open Selected" and "Create New Company..." buttons at the bottom.</li>
	 * </ul>
	 * It also configures cell factories for the list view and adds a listener to update the
	 * preview area when the list selection changes.
	 */
	private void buildUI()
	{
                /* LEFT list */
                this.companyList.setItems(this.companies);
                this.companyList.setCellFactory(v -> new ListCell<>()
                {
                        @Override protected void updateItem(Company c, boolean empty)
                        {
                                super.updateItem(c, empty);
                                setText(empty || c == null ? null : c.getName());
                        }

                });
                this.companyList.getSelectionModel().selectedItemProperty()
                        .addListener((obs, o, n) -> showPreview(n));
                ScrollPane listPane = new ScrollPane(this.companyList);
		listPane.setFitToHeight(true);
		listPane.setFitToWidth(true);
		listPane.setPadding(new Insets(5));
		listPane.setPrefWidth(300);
		listPane.setStyle("-fx-border-color: lightgray;");
		
		/* RIGHT preview */
		this.previewArea.setEditable(false);
		ScrollPane previewPane = new ScrollPane(this.previewArea);
		previewPane.setFitToHeight(true);
		previewPane.setFitToWidth(true);
		previewPane.setPadding(new Insets(5));
		previewPane.setStyle("-fx-border-color: lightgray;");
		
		SplitPane split = new SplitPane(listPane, previewPane);
		split.setDividerPositions(0.35);
		setCenter(split);
		
		/* buttons */
		Button openBtn = new Button("Open Selected");
		Button createBtn = new Button("Create New Company…");
		openBtn.setOnAction(e -> openSelected());
		createBtn.setOnAction(e -> createNew());
		HBox buttons = new HBox(10, openBtn, createBtn);
		buttons.setPadding(new Insets(8));
		setBottom(buttons);
	}
	
        /**
         * Reloads the list of companies displayed in the {@code companyList}.
         * Retrieves all companies from the database and selects the first
         * entry if available.
         */
        private void reloadCompanyList()
        {
                this.companies.clear();
                this.companies.addAll(this.db.listCompanies());

                if (!this.companies.isEmpty())
                {
                        this.companyList.getSelectionModel().selectFirst();
                }

        }

        /**
         * Displays information about the selected company in the preview area.
         *
         * @param c The selected {@link Company}.
         */
        private void showPreview(Company c)
        {

                if (c == null)
                {
                        this.previewArea.clear();
                        return;
                }

                if (c.getCompanyProfileModel() != null)
                {
                        this.previewArea.setText("Company: " +
                                c.getCompanyProfileModel().getCompanyName());
                }
                else
                {
                        this.previewArea.setText("Company: (no profile)");
                }

        }
	
        /**
         * Handles the action to "open" the company currently selected in the {@code companyList}.
         * Notifies the optional {@link OnCompanyOpenedHandler} after loading the company
         * into {@link CurrentCompany}.
         */
        void openSelected() // Package-private
        {
                Company sel = this.companyList.getSelectionModel().getSelectedItem();

                if (sel == null)
                {
                        AlertBox.showWarning(getScene().getWindow(), "No Company Selected");
                        return;
                }

                CurrentCompany.forceCompanyLoad(sel);

                if (this.companyOpenedHandler != null)
                {
                        this.companyOpenedHandler.onCompanyOpened(sel);
                }

        }
	
	/**
	 * Sets the handler to be called when a company is opened.
	 * @param handler The handler to set.
	 */
	public void setOnCompanyOpenedHandler(OnCompanyOpenedHandler handler)
	{
		this.companyOpenedHandler = handler;
	}
	
        /**
         * Initiates the process of creating a new company. This method opens a new
         * {@link Stage} containing a {@link CreateOrEditCompanyPanelFX} wizard for
         * entering company details. After the company is saved, the list of
         * companies is refreshed and the new company is loaded into
         * {@link CurrentCompany}.
         */
        private void createNew()
        {
                Stage dlg = new Stage();
                dlg.initOwner(this.getScene() != null ? this.getScene().getWindow() : null);
                dlg.setTitle("Create New Company");

                CreateOrEditCompanyPanelFX form = new CreateOrEditCompanyPanelFX(null, model -> {
                        Company newCompany = new Company();
                        newCompany.setCompanyProfileModel(model);
                        this.db.saveCompany(newCompany);
                        CurrentCompany.forceCompanyLoad(newCompany);
                        if (this.companyOpenedHandler != null)
                        {
                                this.companyOpenedHandler.onCompanyOpened(newCompany);
                        }
                        dlg.close();
                        reloadCompanyList();
                });
                dlg.setScene(new Scene(form, 800, 600));
                dlg.show();
        }
	
}
