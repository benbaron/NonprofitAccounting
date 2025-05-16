
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
	
	private final DocumentStorageService service;
	
	/* UI fields */
	private final TextField txnIdField = new TextField();
	private final TextField chosenFileField = new TextField();
	private File chosen;
	
	public DocumentsPanelFX(DocumentStorageService service)
	{
		this.service = service;
		setPadding(new Insets(10));
		buildUI();
	}
	
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
	 * 
	 * @return
	 */
	private Stage getSceneWindow()
	{
		Scene s = getScene();
		return s != null ? (Stage) s.getWindow() : null;
	}
	
	/**
	 * 
	 * @param msg
	 */
	private static void alert(String msg)
	{
		new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
	}
	
}
