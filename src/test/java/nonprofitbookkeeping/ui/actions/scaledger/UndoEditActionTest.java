package nonprofitbookkeeping.ui.actions.scaledger;

import org.junit.jupiter.api.Test;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;

import static org.junit.jupiter.api.Assertions.*;

public class UndoEditActionTest {
    @Test
    public void testActionUndoesEditWhenAvailable() {
        UndoManager manager = new UndoManager();
        final boolean[] undone = { false };
        manager.addEdit(new AbstractUndoableEdit() {
            @Override
            public void undo() {
                super.undo();
                undone[0] = true;
            }
        });

        UndoEditAction action = new UndoEditAction(manager);
        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "undo"));
        assertTrue(undone[0], "Edit should be undone when available");
    }

    @Test
    public void testActionShowsDialogWhenNothingToUndo() {
        UndoManager manager = new UndoManager();
        UndoEditAction action = new UndoEditAction(manager);
        // No edits added; just ensure no exception and nothing undone
        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "undo"));
        assertFalse(manager.canUndo(), "Manager should still have nothing to undo");
    }
}
