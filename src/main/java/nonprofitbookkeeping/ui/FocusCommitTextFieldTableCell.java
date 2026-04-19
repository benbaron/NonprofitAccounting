package nonprofitbookkeeping.ui;

import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 * Reusable table cell that commits text edits when focus leaves the editor.
 */
public class FocusCommitTextFieldTableCell<S> extends TextFieldTableCell<S, String>
{
    private TextField textField;
    private boolean listenerInstalled;

    @Override
    public void startEdit()
    {
        super.startEdit();
        if (textField == null)
        {
            textField = (TextField) getGraphic();
        }
        if (textField != null && !listenerInstalled)
        {
            listenerInstalled = true;
            textField.focusedProperty().addListener((obs, oldValue, newValue) ->
            {
                if (!newValue && isEditing())
                {
                    commitEdit(textField.getText());
                }
            });
        }
    }
}
