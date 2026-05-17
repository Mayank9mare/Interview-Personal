// Companies: Microsoft, Google, Amazon, Uber, Atlassian
package com.uber.texteditor;

import java.util.*;

/**
 * In-memory multi-line text editor with full undo/redo support.
 *
 * <p>Implements the Command pattern: every mutating operation is encapsulated as a
 * {@link Command} and pushed onto an undo stack. Undo reverses the last command and
 * moves it to a redo stack; redo re-executes it.  Any new edit clears the redo stack.
 *
 * <p>Data structures: {@code List<StringBuilder>} for the document rows; two
 * {@code ArrayDeque}s as LIFO stacks for undo and redo history.
 *
 * <p>Not thread-safe.
 */
public class TextEditor {
    /**
     * A reversible editor operation.
     */
    interface Command {
        /** Applies the operation to the document. */
        void execute(List<StringBuilder> rows);
        /** Reverses a previously executed operation. */
        void undo(List<StringBuilder> rows);
    }

    /** Inserts text (and optionally a new row) at a given position. */
    private static class AddText implements Command {
        private final int row, col;
        private final String text;
        private final boolean newRow;
        AddText(int row, int col, String text, boolean newRow) {
            this.row = row; this.col = col; this.text = text; this.newRow = newRow;
        }
        public void execute(List<StringBuilder> rows) {
            if (newRow) rows.add(new StringBuilder());
            rows.get(row).insert(col, text);
        }
        public void undo(List<StringBuilder> rows) {
            rows.get(row).delete(col, col + text.length());
            if (newRow) rows.remove(row);
        }
    }

    /** Removes a substring and records it for undo. */
    private static class DeleteText implements Command {
        private final int row, col, length;
        private String deleted;
        DeleteText(int row, int col, int length) { this.row = row; this.col = col; this.length = length; }
        public void execute(List<StringBuilder> rows) {
            StringBuilder sb = rows.get(row);
            deleted = sb.substring(col, col + length);
            sb.delete(col, col + length);
        }
        public void undo(List<StringBuilder> rows) { rows.get(row).insert(col, deleted); }
    }

    /** Document contents; each element is one line. */
    private final List<StringBuilder> rows = new ArrayList<>();
    /** Commands that can be undone (LIFO). */
    private final Deque<Command> undoStack = new ArrayDeque<>();
    /** Commands that can be redone after an undo (LIFO). */
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * Inserts {@code text} at the given position.
     *
     * <p>If {@code row} equals the current number of rows a new row is appended first.
     *
     * @param row    zero-based row index (may equal {@code rows.size()} to append a new line)
     * @param column zero-based column index for the insertion point
     * @param text   text to insert
     */
    public void addText(int row, int column, String text) {
        boolean newRow = (row == rows.size());
        Command cmd = new AddText(row, column, text, newRow);
        cmd.execute(rows);
        undoStack.push(cmd);
        redoStack.clear();
    }

    /**
     * Deletes {@code length} characters starting at the given position.
     *
     * @param row         zero-based row index
     * @param startColumn zero-based column index of the first character to delete
     * @param length      number of characters to remove
     */
    public void deleteText(int row, int startColumn, int length) {
        Command cmd = new DeleteText(row, startColumn, length);
        cmd.execute(rows);
        undoStack.push(cmd);
        redoStack.clear();
    }

    /**
     * Reverts the most recent edit. No-op if there is nothing to undo.
     */
    public void undo() {
        if (undoStack.isEmpty()) return;
        Command cmd = undoStack.pop();
        cmd.undo(rows);
        redoStack.push(cmd);
    }

    /**
     * Re-applies the most recently undone edit. No-op if there is nothing to redo.
     */
    public void redo() {
        if (redoStack.isEmpty()) return;
        Command cmd = redoStack.pop();
        cmd.execute(rows);
        undoStack.push(cmd);
    }

    /**
     * @param row zero-based row index
     * @return the current content of that row as a plain {@code String}
     */
    public String readLine(int row) { return rows.get(row).toString(); }
}
