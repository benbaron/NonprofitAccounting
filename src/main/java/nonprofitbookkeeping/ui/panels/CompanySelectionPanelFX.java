
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.CompanyLoaderService;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.helpers.AlertBox;

/**
 * A JavaFX panel that allows users to select an existing ".npbk" company file,
 * preview basic information about it (by attempting to load it into {@link CurrentCompany}),
 * open the selected company, or initiate the creation of a new company.
 * It displays a list of available company files from a default directory and
 * uses a {@link TextArea} for previews.
 * <p>
 * Note: The inner class {@code OnCompanyOpenedHandler} is currently a stub and not a
 * functional interface as might be expected for typical JavaFX callback mechanisms.
 * The "preview" functionality currently involves fully loading the company data.
 * </p>
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
	public class OnCompanyOpenedHandler
	{
		
		/**
		 * Intended to be called when a company's data has been successfully loaded or processed.
		 * Note: This is a stub implementation and currently does nothing.
		 *
		 * @param company The {@link Company} object that was opened or processed.
		 */
		public void onCompanyOpened(Company company)
		{
			// TODO Auto-generated method stub: Implement callback logic to notify the
			// application that a company has been opened or processed. This might involve updating the
			// main UI, enabling/disabling features, etc.
		}
		
	}
	
	/** ListView component to display the list of discoverable ".npbk" company files. */
	private final ListView<File> companyList = new ListView<>();
	/** ObservableList that backs the {@code companyList}, holding the {@link File} objects. */
	private final ObservableList<File> npbkFiles = FXCollections.observableArrayList();
	/** TextArea used to display a preview or details of the company file selected in the {@code companyList}. */
	private final TextArea previewArea = new TextArea();
	
	/** Handler for when a company is successfully opened. */
	private OnCompanyOpenedHandler companyOpenedHandler;
	
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
	 *   <li>A {@link ListView} ({@code companyList}) on the left to display company files.</li>
	 *   <li>A {@link TextArea} ({@code previewArea}) on the right for showing details of the selected file.</li>
	 *   <li>A {@link SplitPane} to manage the layout of the list and preview area.</li>
	 *   <li>"Open Selected" and "Create New Company..." buttons at the bottom.</li>
	 * </ul>
	 * It also configures cell factories for the list view and adds a listener to update the
	 * preview area when the list selection changes.
	 */
	private void buildUI()
	{
		/* LEFT list */
		this.companyList.setItems(this.npbkFiles);
		this.companyList.setCellFactory(v -> new ListCell<>()
		{
			@Override protected void updateItem(File f, boolean empty)
			{
				super.updateItem(f, empty);
				setText(empty || f == null ? null : f.getName());
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
	 * Reloads the list of company files displayed in the {@code companyList}.
	 * It first clears the existing items, then determines the default company directory
	 * using {@link PreferencesService#getDefaultCompanyDir()}. If this directory doesn't exist,
	 * it attempts to create it. Finally, it populates the list with ".npbk" files found
	 * in that directory via {@link CompanyLoaderService#findCompanyFiles(File)}.
	 * If any files are found, the first one in the list is automatically selected.
	 */
	private void reloadCompanyList()
	{
		this.npbkFiles.clear();
		File dir = new File(PreferencesService.getDefaultCompanyDir());
		
		if (!dir.exists())
		{
			dir.mkdirs(); // Create the directory if it doesn't exist.
		}
		
		this.npbkFiles.addAll(CompanyLoaderService.findCompanyFiles(dir));
		
		if (!this.npbkFiles.isEmpty())
		{
			this.companyList.getSelectionModel().selectFirst(); // Auto-select the first item.
		}
		
	}
	
	/**
	 * Attempts to load the selected company file {@code f} into the {@link CurrentCompany} context
	 * and displays some information in the preview area.
	 * <p>
	     * Note: This method effectively "opens" the company by calling {@link CurrentCompany#loadFromPersistent(File)}
	     * and {@link CurrentCompany#markCompanyOpen()} merely for previewing. This might have unintended side effects if the user
	 * does not proceed to click the "Open Selected" button. A less invasive preview mechanism
	 * (e.g., reading only metadata) might be preferable.
	 * </p>
	 * If loading fails, an error alert is shown, and the preview area indicates failure.
	 * If {@code f} is null, the preview area is cleared.
	 * 
	 * @param f The {@link File} selected in the list to be "previewed".
	 */
	private void showPreview(File f)
	{
		
		if (f == null)
		{
			this.previewArea.clear();
			// Optionally, also clear CurrentCompany if previewing means loading into it.
			// CurrentCompany.close(); // Or a more nuanced unload without full close.
			return;
		}
		
		try
		{
			// This "preview" actually loads and opens the company.
			// For a true preview without side effects, it should parse metadata or summary
			// without altering CurrentCompany state until "Open Selected" is clicked.
			CurrentCompany.loadFromPersistent(f); // This can throw various exceptions.
			CurrentCompany.markCompanyOpen(); // Sets the company as globally open.

			// The previewArea is not explicitly updated here with company details from
			// CurrentCompany.
			// It might be intended to show details from CurrentCompany after it's loaded.
			// For example:
			
			if (CurrentCompany.getCompany() != null &&
				CurrentCompany.getCompany().getCompanyProfileModel() != null)
			{
				this.previewArea.setText("Company: " +
					CurrentCompany.getCompany().getCompanyProfileModel().getCompanyName() +
					"\nFile: " + f.getName());
			}
			else
			{
				this.previewArea.setText(
					"Preview for: " + f.getName() + "\n(Could not load full details for preview)");
			}
			
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			this.previewArea.setText(
				"Could not load preview for: " + f.getName() + "\nError: " + e.getMessage());
			AlertBox.showError(null, "File Load Failed: " + e.getMessage());
		}
		
	}
	
	/**
	 * Handles the action to "open" the company file currently selected in the {@code companyList}.
	 * <p>
	 * If a file is selected, it displays an informational {@link Alert}.
	 * Note: The actual loading and setting of the {@link CurrentCompany} is performed by
	 * the {@link #showPreview(File)} method when a list item is selected. This method's primary
	 * role seems to be user confirmation or triggering subsequent application-level actions.
	 * A TODO comment indicates that it should notify an application controller, which is currently
	 * not implemented here.
	 * </p>
	 */
	void openSelected() // Package-private
	{
		File sel = this.companyList.getSelectionModel().getSelectedItem();
		
		if (sel == null)
		{
			AlertBox.showWarning(getScene().getWindow(),
				"No Company Selected");
			return;
		}
		
		// Assuming showPreview already loaded it into CurrentCompany and set it as
		// open.
		// If not, the loading logic would be here.
		Alert alert = new Alert(Alert.AlertType.INFORMATION,
			"Opening company: " + sel.getName() +
				"\n(Note: Company might have already been loaded for preview).");
		alert.initOwner(this.getScene() != null ? this.getScene().getWindow() : null);
		alert.showAndWait();
		
		if (this.companyOpenedHandler != null && CurrentCompany.getCompany() != null)
		{
			this.companyOpenedHandler.onCompanyOpened(CurrentCompany.getCompany());
		}
		else
		{
			
			if (this.companyOpenedHandler == null)
			{
				System.err.println(
					"CompanySelectionPanelFX: companyOpenedHandler is null. Cannot notify.");
			}
			
			if (CurrentCompany.getCompany() == null)
			{
				System.err.println(
					"CompanySelectionPanelFX: CurrentCompany.getCompany() is null. Cannot notify with company data.");
			}
			
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
	 * Initiates the process of creating a new company.
	 * This method opens a new {@link Stage} containing a {@link CreateOrEditCompanyPanelFX}
	 * instance. The {@code CreateOrEditCompanyPanelFX} handles the actual data input for the new company.
	 * Upon successful creation and closing of that dialog, this method calls {@link #reloadCompanyList()}
	 * to refresh the list of company files, which should now include the newly created company's file.
	 */
	private void createNew()
	{
		// Reuse the CreateOrEditCompanyPanelFX in a new dialog
		Stage dlg = new Stage();
		dlg.initOwner(this.getScene() != null ? this.getScene().getWindow() : null); // Set owner
		dlg.setTitle("Create New Company");
		
		CreateOrEditCompanyPanelFX form =
			new CreateOrEditCompanyPanelFX(null,
				model ->
				{
					dlg.close();
					reloadCompanyList();
				});
		dlg.setScene(new Scene(form, 800, 600));
		dlg.show();
	}
	
}
