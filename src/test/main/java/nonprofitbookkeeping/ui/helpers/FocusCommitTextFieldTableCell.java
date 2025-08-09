
package nonprofitbookkeeping.ui.helpers;

import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

/**
 * A custom {@link TextFieldTableCell} that commits the edit when the TextField loses focus.
 * Standard {@code TextFieldTableCell} typically requires an explicit action like pressing Enter
 * to commit the edit. This class provides a more intuitive behavior for users who
 * expect changes to be saved when they click or tab away from the cell.
 *
 * @param <S> The type of the TableView generic type (i.e., S == TableView&lt;S&gt;).
 * @param <T> The type of the item contained within the cell.
 */
public class FocusCommitTextFieldTableCell<S, T> extends TextFieldTableCell<S, T>
{
	
	/**
	 * Creates a {@code FocusCommitTextFieldTableCell} that uses the provided {@link StringConverter}
	 * to convert between the string representation in the TextField and the object representation
	 * of the cell item.
	 *
	 * @param converter The {@link StringConverter} to use for converting the text input.
	 *                  If null, a default converter for the type T might be used if T is String,
	 *                  otherwise, editing might not work as expected for non-String types without a converter.
	 */
	public FocusCommitTextFieldTableCell(StringConverter<T> converter)
	{
		super(converter);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Overrides the default edit starting behavior to add a focus listener to the
	 * underlying {@link TextField}. When the TextField loses focus ({@code newFocusValue} is false),
	 * this listener attempts to convert the TextField's current text using the cell's
	 * {@link StringConverter} and then calls {@link #commitEdit(Object)} with the converted value.
	 * </p>
	 * <p>
	 * If the conversion from string to type {@code T} fails (e.g., due to an invalid format),
	 * an error message is printed to standard error, and {@link #cancelEdit()} is called
	 * to revert the cell to its previous value, mimicking the behavior of pressing Escape.
	 * </p>
	 */
	@Override public void startEdit()
	{
		super.startEdit();
		
		if (isEditing())
		{
			TextField textField = (TextField) getGraphic(); // getGraphic() should return the
															// TextField after super.startEdit()
			
			if (textField != null)
			{
				textField.focusedProperty().addListener((obs, oldFocusValue, newFocusValue) -> {
					
					if (!newFocusValue)
					{ // If focus is lost
						// It's important to get the current text from the TextField
						// and convert it using the cell's converter.
						String text = textField.getText();
						
						try
						{
							// Commit the edit with the converted value
							commitEdit(getConverter().fromString(text));
						}
						catch (Exception e)
						{
							// Handle cases where conversion might fail, e.g., invalid number
							// format.
							// For now, we'll cancel edit to revert to the old value if conversion
							// fails.
							// A more sophisticated handling might involve showing an error to the
							// user.
							// This matches default TextFieldTableCell behavior on failed commit
							// (e.g. pressing Esc).
							System.err.println("Conversion/Commit failed for value: " + text +
								". Error: " + e.getMessage()); // Consider using a logger
							cancelEdit(); // Revert to the previous value
						}
						
					}
					
				});
			}
			
		}
		
	}
	
}
