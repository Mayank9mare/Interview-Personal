// Companies: Microsoft, Google, Amazon, Uber, Atlassian
package com.uber.texteditor;

import java.util.*;

public class TextEditor {
    interface Command {
        void execute(List<StringBuilder> rows);
        void undo(List<StringBuilder> rows);
    }

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

    private final List<StringBuilder> rows = new ArrayList<>();
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void addText(int row, int column, String text) {
        boolean newRow = (row == rows.size());
        Command cmd = new AddText(row, column, text, newRow);
        cmd.execute(rows);
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void deleteText(int row, int startColumn, int length) {
        Command cmd = new DeleteText(row, startColumn, length);
        cmd.execute(rows);
        undoStack.push(cmd);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        Command cmd = undoStack.pop();
        cmd.undo(rows);
        redoStack.push(cmd);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        Command cmd = redoStack.pop();
        cmd.execute(rows);
        undoStack.push(cmd);
    }

    public String readLine(int row) { return rows.get(row).toString(); }
}
