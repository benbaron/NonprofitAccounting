
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
 * JavaFX version of {@code CompanySelectionPanel}. Lets the user pick an .npbk
 * company file, preview its profile, open it, or create a new company.
 */
public class CompanySelectionPanelFX extends BorderPane
{
	
	private final ListView<File> companyList = new ListView<>();
	private final ObservableList<File> npbkFiles = FXCollections.observableArrayList();
	private final TextArea previewArea = new TextArea();
	
	/**
	 * 
	 * Constructor CompanySelectionPanelFX
	 */
	public CompanySelectionPanelFX()
	{
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
	 * 
	 */
	private void openSelected()
	{
		File sel = this.companyList.getSelectionModel().getSelectedItem();
		if (sel == null)
		{
			return;
		}
		Alert a = new Alert(Alert.AlertType.INFORMATION, "Opening company: " + sel.getName());
		a.showAndWait();
		// TODO: notify application controller
	}
	
	/**
	 * 
	 */
	private void createNew()
	{
		// Reuse the CreateCompanyPanelFX in a new dialog
		Stage dlg = new Stage();
		dlg.setTitle("Create New Company");
		CreateCompanyPanelFX form = new CreateCompanyPanelFX(null, model -> {
			dlg.close();
			reloadCompanyList();
		});
		dlg.setScene(new Scene(form, 800, 600));
		dlg.show();
	}
	
}
