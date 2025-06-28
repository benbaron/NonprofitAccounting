package nonprofitbookkeeping.ui.actions.scaledger;

import javax.swing.*;
import javax.swing.undo.UndoManager;

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
      if (this.undoManager.canUndo())
      {
        this.undoManager.undo();
      }
      else
      {
        JOptionPane.showMessageDialog(null, "Nothing to undo", "Undo", JOptionPane.INFORMATION_MESSAGE);
      }
    }

    /** The {@link UndoManager} responsible for tracking edits to undo. */
    private final UndoManager undoManager;

    /**
     * Creates a new {@code UndoEditAction} with its own {@link UndoManager}.
     * Components performing undoable edits can obtain this manager via
     * {@link #getUndoManager()} and add their edits to it.
     */
    public UndoEditAction()
    {
      super("Undo Last Edit");
      this.undoManager = new UndoManager();
    }

    /**
     * Creates a new action using the provided {@link UndoManager}.
     *
     * @param manager The manager to use for undo operations. Must not be null.
     */
    public UndoEditAction(UndoManager manager)
    {
      super("Undo Last Edit");
      this.undoManager = manager;
    }

    /**
     * Returns the underlying {@link UndoManager} so that callers can register
     * their {@link javax.swing.undo.UndoableEdit} instances.
     *
     * @return the {@link UndoManager} used by this action
     */
    public UndoManager getUndoManager()
    {
      return this.undoManager;
    }
}
