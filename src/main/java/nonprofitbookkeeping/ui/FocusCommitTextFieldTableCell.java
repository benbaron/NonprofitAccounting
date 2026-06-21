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
        if (this.textField == null)
        {
            this.textField = (TextField) getGraphic();
        }
        if (this.textField != null && !this.listenerInstalled)
        {
            this.listenerInstalled = true;
            this.textField.focusedProperty().addListener((obs, oldValue, newValue) ->
            {
                if (!newValue && isEditing())
                {
                    commitEdit(this.textField.getText());
                }
            });
        }
    }
}
