package nonprofitbookkeeping.ui.util;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Simple manager for storing undo operations.
 * Each undoable action can register a {@link Runnable} that will be executed
 * when an undo is requested.
 */
public final class UndoManager {
    private static final Deque<Runnable> undoStack = new ArrayDeque<>();

    private UndoManager() {}

    /**
     * Records an undo operation.
     * @param operation the runnable to execute when undoing. If {@code null}, nothing is recorded.
     */
    public static void record(Runnable operation) {
        if (operation != null) {
            undoStack.push(operation);
        }
    }

    /**
     * Executes the most recently recorded undo operation if present.
     * If no operation is available, this method simply returns false.
     *
     * @return {@code true} if an operation was undone, otherwise {@code false}.
     */
    public static boolean undoLast() {
        Runnable op = undoStack.poll();
        if (op != null) {
            op.run();
            return true;
        }
        return false;
    }

    /**
     * Clears all recorded undo operations.
     */
    public static void clear() {
        undoStack.clear();
    }
}
