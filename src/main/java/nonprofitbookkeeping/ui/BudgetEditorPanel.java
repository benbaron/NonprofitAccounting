package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Budget editor placeholder panel.
 */
public class BudgetEditorPanel extends BorderPane
{

	/**
	 * Creates the budget editor panel.
	 */
	public BudgetEditorPanel()
	{
		setPadding(new Insets(8));
		Label title = new Label("Budget Editor");
		title.getStyleClass().add("panel-title");

		Button add = new Button("+ Add Budget Line");
		Button save = new Button("Save");
		HBox actions = new HBox(8, add, save);

		setTop(new VBox(6, title, actions, new Separator()));
		setCenter(new Label("TODO: Budget entry grid (inputs)."));
	}

}
