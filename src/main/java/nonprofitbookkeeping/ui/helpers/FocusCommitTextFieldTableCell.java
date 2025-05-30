package nonprofitbookkeeping.ui.helpers;

import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class FocusCommitTextFieldTableCell<S, T> extends TextFieldTableCell<S, T> {

    public FocusCommitTextFieldTableCell(StringConverter<T> converter) {
        super(converter);
    }

    @Override
    public void startEdit() {
        super.startEdit();

        if (isEditing()) {
            TextField textField = (TextField) getGraphic();
            if (textField != null) {
                textField.focusedProperty().addListener((obs, oldFocusValue, newFocusValue) -> {
                    if (!newFocusValue) { // If focus is lost
                        // It's important to get the current text from the TextField
                        // and convert it using the cell's converter.
                        String text = textField.getText();
                        try {
                            // Commit the edit with the converted value
                            commitEdit(getConverter().fromString(text));
                        } catch (Exception e) {
                            // Handle cases where conversion might fail, e.g., invalid number format.
                            // For now, we'll cancel edit to revert to the old value if conversion fails.
                            // A more sophisticated handling might involve showing an error to the user.
                            // This matches default TextFieldTableCell behavior on failed commit (e.g. pressing Esc).
                            System.err.println("Conversion/Commit failed for value: " + text + ". Error: " + e.getMessage());
                            cancelEdit(); // Revert to the previous value
                        }
                    }
                });
            }
        }
    }
}
