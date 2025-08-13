
package nonprofitbookkeeping.ui.helpers;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Simple helper to prompt the user for a start and end date using two
 * {@link DatePicker} controls inside a {@link Dialog}.
 */
public final class DateRangePickerDialog
{
	
	private DateRangePickerDialog()
	{
	}
	
	/**
	 * Displays a dialog with two date pickers and returns the chosen range.
	 *
	 * @param owner the parent window for modality
	 * @param title dialog title
	 * @param startLabel label for the start date picker
	 * @param endLabel label for the end date picker
	 * @return an {@link Optional} containing a {@code LocalDate[]} with
	 *         start and end dates if the user confirmed; otherwise an empty
	 *         optional
	 */
	public static Optional<LocalDate[]> show(	Window owner, String title, String startLabel,
												String endLabel)
	{
		Dialog<LocalDate[]> dlg = new Dialog<>();
		dlg.initOwner(owner);
		dlg.setTitle(title);
		
		DatePicker startPicker = new DatePicker();
		DatePicker endPicker = new DatePicker();
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.add(new Label(startLabel), 0, 0);
		grid.add(startPicker, 1, 0);
		grid.add(new Label(endLabel), 0, 1);
		grid.add(endPicker, 1, 1);
		
		dlg.getDialogPane().setContent(grid);
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		dlg.setResultConverter(btn -> {
			
			if (btn == ButtonType.OK)
			{
				return new LocalDate[]
				{ startPicker.getValue(), endPicker.getValue() };
			}
			
			return null;
		});
		
		return dlg.showAndWait();
	}
	
}
