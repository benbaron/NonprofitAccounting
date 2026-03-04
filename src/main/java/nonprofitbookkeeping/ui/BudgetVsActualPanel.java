package nonprofitbookkeeping.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Budget versus actual placeholder panel.
 */
public class BudgetVsActualPanel extends BorderPane
{

	/**
	 * Creates the budget versus actual panel.
	 */
	public BudgetVsActualPanel()
	{
		setPadding(new Insets(8));
		Label title = new Label("Budget vs Actual");
		title.getStyleClass().add("panel-title");

		Button run = new Button("Run");
		Button export = new Button("Export");
		HBox actions = new HBox(8, run, export);

		setTop(new VBox(6, title, actions, new Separator()));
		setCenter(new Label("TODO: Budget vs Actual report (outputs)."));
	}

}
