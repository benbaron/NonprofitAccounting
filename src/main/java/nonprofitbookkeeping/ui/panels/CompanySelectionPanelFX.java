
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
import nonprofitbookkeeping.model.Company; // Added import for OnCompanyOpenedHandler
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.CompanyLoaderService;
import nonprofitbookkeeping.service.PreferencesService;
import nonprofitbookkeeping.ui.helpers.AlertBox;

/**
 * JavaFX version of {@code CompanySelectionPanel}. Lets the user pick an .npbk
 * company file, preview its profile, open it, or create a new company.
 * This panel uses a callback mechanism ({@link OnCompanyOpenedHandler}) to notify
 * the application when a company has been successfully opened.
 */
public class CompanySelectionPanelFX extends BorderPane
{
	/**
     * Functional interface for a callback to be invoked when a company
     * has been successfully opened or processed by this panel.
     */
    @FunctionalInterface
    public static interface OnCompanyOpenedHandler {
        /**
         * Called when a company's data has been successfully loaded/processed.
         *
         * @param company The {@link Company} object that was opened or processed.
         */
        void onCompanyOpened(Company company);
    }
	
	private final ListView<File> companyList = new ListView<>();
	private final ObservableList<File> npbkFiles = FXCollections.observableArrayList();
	private final TextArea previewArea = new TextArea();
	private final OnCompanyOpenedHandler companyOpenedHandler;
	
	/**
	 * Constructs a new CompanySelectionPanelFX.
	 *
	 * @param openedHandler The handler to be called when a company is successfully opened
     *                      or processed. Must not be null.
     * @throws IllegalArgumentException if openedHandler is null.
	 */
	public CompanySelectionPanelFX(OnCompanyOpenedHandler openedHandler)
	{
		if (openedHandler == null) {
            throw new IllegalArgumentException("OnCompanyOpenedHandler cannot be null.");
        }
        this.companyOpenedHandler = openedHandler;

		setPadding(new Insets(10));
		buildUI();
		reloadCompanyList();
	}
	
	/* --------------------------------------------------------------------- */
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
	
	/* --------------------------------------------------------------------- */
	private void reloadCompanyList()
	{
		this.npbkFiles.clear();
		File dir = new File(PreferencesService.getDefaultCompanyDir());
		
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		
		this.npbkFiles.addAll(CompanyLoaderService.findCompanyFiles(dir));
		
		if (!this.npbkFiles.isEmpty())
		{
			this.companyList.getSelectionModel().selectFirst();
		}
		
	}
	
	/**
	 * 
	 * @param f
	 */
	private void showPreview(File f)
	{
		
		if (f == null)
		{
			this.previewArea.clear();
			return;
		}
		
		try
		{
			CurrentCompany.loadFromPersistent(f);
			CurrentCompany.open();
		}
		catch (IOException | ActionCancelledException | NoFileCreatedException e)
		{
			AlertBox.showError(null, "File Load Failed");
		}
		
	}
	
	/**
	 * Handles opening the selected company file.
	 * It loads the company data from the persistent file, opens it via {@link CurrentCompany},
	 * and then invokes the {@code companyOpenedHandler} callback with the loaded company.
	 * Errors during loading or opening are displayed to the user via an alert dialog.
	 */
	private void openSelected()
	{
		File selectedFile = this.companyList.getSelectionModel().getSelectedItem();
		
		if (selectedFile == null)
		{
			return; // No file selected, do nothing.
		}
		
		try {
            CurrentCompany.loadFromPersistent(selectedFile);
            CurrentCompany.open(); // This should trigger listeners and update CurrentCompany.getCompany()

            Company openedCompany = CurrentCompany.getCompany();

            if (openedCompany != null) {
                // Notify the registered handler that a company has been successfully opened.
                this.companyOpenedHandler.onCompanyOpened(openedCompany);
            } else {
                // This case should ideally not happen if loadFromPersistent and open succeed
                // without exceptions but CurrentCompany.getCompany() is still null.
                AlertBox.showError("Company Open Error",
                                   "Failed to retrieve company data after attempting to load and open the file. " +
                                   "The company object is unexpectedly null.");
            }
        } catch (IOException | ActionCancelledException | NoFileCreatedException e) {
            AlertBox.showError("Error Opening Company",
                               "Failed to load company data from file: " + selectedFile.getName() +
                               "\nError: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions during the process
            AlertBox.showError("Unexpected Error",
                               "An unexpected error occurred while opening the company: " + e.getMessage());
        }
	}
	
	/**
	 * 
	 */
	private void createNew()
	{
		// Reuse the CreateCompanyPanelFX in a new dialog
		Stage dlg = new Stage();
		dlg.setTitle("Create New Company");
		
		CreateCompanyPanelFX form =
			new CreateCompanyPanelFX(null,
				model ->
				{
					dlg.close();
					reloadCompanyList();
				});
		dlg.setScene(new Scene(form, 800, 600));
		dlg.show();
	}
	
}
