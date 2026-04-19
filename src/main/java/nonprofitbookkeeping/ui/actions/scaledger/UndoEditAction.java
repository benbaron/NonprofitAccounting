
package nonprofitbookkeeping.ui.actions.scaledger;

import javax.swing.*;
import javax.swing.undo.UndoManager;

import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Swing Action intended to trigger an "undo" operation for the last edit
 * performed in a viewer, presumably a table viewer like one managed by {@code PageViewerPanel}
 * or {@code XlsmTableViewerFX}.
 * <p>
 * This class extends {@link javax.swing.AbstractAction}, making it suitable for Swing UIs.
 * It exposes an {@link UndoManager} so UI components can register their
 * {@link javax.swing.undo.UndoableEdit} instances. When triggered, the action
 * undoes the most recent edit if possible and otherwise notifies the user that
 * there is nothing to undo.
 * </p>
 */
public class UndoEditAction extends AbstractAction
{
	private static final Logger LOGGER = LoggerFactory.getLogger(UndoEditAction.class);

	/**
	 * The unique identifier for this serializable class.
	 */
	private static final long serialVersionUID = -3286001686985951299L;
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is called when the undo action is triggered (for example by a menu item or button click in a Swing UI).
	 * It checks the underlying {@link UndoManager} and undoes the most recent edit if possible.
	 * If no edits are available, it simply notifies the user via a dialog.
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
			LOGGER.info("Popup [INFORMATION] title='{}' content='{}'",
				"Undo",
				"Nothing to undo");
			JOptionPane.showMessageDialog(null, "Nothing to undo", "Undo",
				JOptionPane.INFORMATION_MESSAGE);
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
