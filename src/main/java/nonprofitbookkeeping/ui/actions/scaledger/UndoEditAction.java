package nonprofitbookkeeping.ui.actions.scaledger;

import javax.swing.*;

import java.awt.event.ActionEvent;
import nonprofitbookkeeping.ui.util.UndoManager;

/**
 * Represents a Swing Action intended to trigger an "undo" operation for the last edit
 * performed in a viewer, presumably a table viewer like one managed by {@code PageViewerPanel}
 * or {@code XlsmTableViewerFX}.
 * <p>
 * Note: This class extends {@link javax.swing.AbstractAction}, making it suitable for Swing UIs.
 * The {@link #actionPerformed(ActionEvent)} method is currently a stub and does not
 * implement any undo logic.
 * </p>
 */
public class UndoEditAction extends AbstractAction
{
    /**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -3286001686985951299L;

	/**
     * {@inheritDoc}
     * <p>
     * This method is called when the undo action is triggered (e.g., by a menu item or button click
     * in a Swing UI).
     * Note: The current implementation is a stub and does not perform any undo operation.
     * A functional implementation would need to interact with a component that supports
     * an undo mechanism (e.g., by tracking edits or using an UndoManager).
     * </p>
     * @param e The {@link ActionEvent} that occurred.
     */
	@Override
    public void actionPerformed(ActionEvent e)
    {
        boolean undone = UndoManager.undoLast();
        if (!undone)
        {
            JOptionPane.showMessageDialog(null,
                    "Nothing to undo", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
