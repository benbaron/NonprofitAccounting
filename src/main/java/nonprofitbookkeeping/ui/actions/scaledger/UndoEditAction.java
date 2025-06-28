package nonprofitbookkeeping.ui.actions.scaledger;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;

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

    /** Manager responsible for tracking undoable edits. */
    private final UndoManager undoManager;

    /**
     * Creates an {@code UndoEditAction} with the provided {@link UndoManager}.
     *
     * @param undoManager the manager that will perform undo operations when
     *                    this action is triggered. May be {@code null}.
     */
    public UndoEditAction(UndoManager undoManager)
    {
        super("Undo");
        this.undoManager = undoManager;
    }

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
        if (this.undoManager != null && this.undoManager.canUndo())
        {
            this.undoManager.undo();
        }
        else
        {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
