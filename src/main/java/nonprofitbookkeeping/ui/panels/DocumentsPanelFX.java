
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.io.IOException;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.service.DocumentStorageService;

/**
 * JavaFX port of the Swing {@code DocumentsPanel}. Lets users select a file on
 * disk and attach it to a transaction ID via {@link DocumentStorageService}.
 */
public class DocumentsPanelFX extends BorderPane
{
	
	/** The service responsible for handling document storage operations. */
	private final DocumentStorageService service;
	
	/* UI fields */
	/** TextField for user to input the transaction ID to associate the document with. */
	private final TextField txnIdField = new TextField();
	/** TextField to display the name of the file chosen by the user. This field is not editable directly. */
	private final TextField chosenFileField = new TextField();
	/** Holds the {@link File} object selected by the user via the FileChooser. Null if no file is chosen. */
	private File chosen;
	
	/**
	 * Constructs a new {@code DocumentsPanelFX}.
	 * Initializes the panel with the necessary {@link DocumentStorageService} and builds the user interface.
	 *
	 * @param service The {@link DocumentStorageService} to be used for attaching documents to transactions. Must not be null.
	 */
	public DocumentsPanelFX(DocumentStorageService service)
	{
		this.service = service;
		setPadding(new Insets(10));
		buildUI();
	}
	
	/**
	 * Builds the user interface of the panel.
	 * This includes a {@link FlowPane} containing labels, text fields for transaction ID and chosen file,
	 * and buttons for choosing a file and attaching it. This pane is then wrapped in a non-collapsible
	 * {@link TitledPane} and set as the top content of this {@link BorderPane}.
	 */
	private void buildUI()
	{
		FlowPane pane = new FlowPane(10, 10);
		pane.setPadding(new Insets(10));
		pane.getChildren().addAll(
			new Label("Transaction ID:"), this.txnIdField,
			chooseBtn(), attachBtn(), this.chosenFileField);
		this.chosenFileField.setEditable(false);
		this.chosenFileField.setPrefWidth(250);
		setTop(new TitledPane("Attach Document", pane)
		{
			{
				setCollapsible(false);
			}
			
		});
	}
	
	/**
	 * Creates and configures the "Choose File" button.
	 * When clicked, this button opens a {@link FileChooser} allowing the user to select a file.
	 * If a file is selected, the {@link #chosen} field is updated, and the file's name is displayed
	 * in the {@link #chosenFileField}.
	 *
	 * @return The configured "Choose File" {@link Button}.
	 */
	private Button chooseBtn()
	{
		Button b = new Button("Choose File");
		b.setOnAction(e -> {
			FileChooser fc = new FileChooser();
			File f = fc.showOpenDialog(getSceneWindow());
			
			if (f != null)
			{
				this.chosen = f;
				this.chosenFileField.setText(f.getName());
			}
			
		});
		return b;
	}
	
	/**
	 * Creates and configures the "Attach" button.
	 * When clicked, this button attempts to attach the {@link #chosen} file to the transaction ID
	 * entered in {@link #txnIdField}, using the {@link #service}.
	 * It performs validation to ensure a file is chosen and a transaction ID is provided.
	 * It displays alerts for success or failure of the attachment process.
	 * On successful attachment, it clears the input fields and the chosen file.
	 *
	 * @return The configured "Attach" {@link Button}.
	 */
	private Button attachBtn()
	{
		Button b = new Button("Attach");
		b.setOnAction(e -> {
			
			if (this.chosen == null || this.txnIdField.getText().isBlank())
			{
				alert("Please choose a file and enter a transaction ID.");
				return;
			}
			
			try
			{
				this.service.attachDocumentToTransaction(this.txnIdField.getText().trim(), this.chosen);
				alert("Document attached.");
				// reset
				this.chosen = null;
				this.chosenFileField.clear();
				this.txnIdField.clear();
			}
			catch (IOException ex)
			{
				alert("Failed to attach: " + ex.getMessage());
			}
			
		});
		return b;
	}
	
	/**
	 * Utility method to get the {@link Stage} (window) that owns this panel's scene.
	 * This is typically used as the owner window for dialogs like {@link FileChooser} or {@link Alert}.
	 * 
	 * @return The parent {@link Stage} of this panel, or null if the scene or window is not available.
	 */
	private Stage getSceneWindow()
	{
		Scene s = getScene();
		return s != null ? (Stage) s.getWindow() : null;
	}
	
	/**
	 * Displays a simple informational alert dialog with an OK button.
	 * 
	 * @param msg The message to be displayed in the alert dialog.
	 */
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
	
}
