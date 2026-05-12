package com.uber.texteditor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextEditorTest {
    private TextEditor ed;
    @BeforeEach void setUp() { ed = new TextEditor(); }

    @Test
    void example1_createRows() {
        ed.addText(0, 0, "hello");
        assertEquals("hello", ed.readLine(0));
        ed.addText(1, 0, "world");
        assertEquals("world", ed.readLine(1));
    }

    @Test
    void example2_insertDeleteUndoRedo() {
        ed.addText(0, 0, "hello");
        ed.addText(1, 0, "world");
        ed.addText(0, 5, "-there");
        assertEquals("hello-there", ed.readLine(0));
        ed.deleteText(0, 5, 6);
        assertEquals("hello", ed.readLine(0));
        ed.undo();
        assertEquals("hello-there", ed.readLine(0));
        ed.redo();
        assertEquals("hello", ed.readLine(0));
    }

    @Test
    void example3_insertMiddle() {
        ed.addText(0, 0, "world");
        ed.addText(0, 5, "-wide web");
        assertEquals("world-wide web", ed.readLine(0));
        ed.deleteText(0, 5, 5);
        ed.undo();
        assertEquals("world-wide web", ed.readLine(0));
    }

    @Test
    void example4_multipleUndoRedoClearedByEdit() {
        ed.addText(0, 0, "hello");
        ed.addText(0, 5, "!");
        ed.addText(0, 6, "!");
        ed.undo();
        ed.undo();
        assertEquals("hello", ed.readLine(0));
        ed.redo();
        assertEquals("hello!", ed.readLine(0));
        ed.addText(0, 6, "?"); // clears redo
        ed.redo(); // no-op
        assertEquals("hello!?", ed.readLine(0));
    }

    @Test
    void example5_deleteAll_emptyRowPersists() {
        ed.addText(0, 0, "aa bb-cc");
        ed.deleteText(0, 0, 8);
        assertEquals("", ed.readLine(0));
        ed.undo();
        assertEquals("aa bb-cc", ed.readLine(0));
    }

    @Test
    void example6_insertAtStart() {
        ed.addText(0, 0, "world");
        ed.addText(0, 0, "hello-");
        assertEquals("hello-world", ed.readLine(0));
    }

    @Test
    void undo_noOp_whenEmpty() {
        ed.addText(0, 0, "hi");
        ed.undo();
        ed.undo(); // no-op, no crash
    }

    @Test
    void redo_noOp_whenEmpty() {
        ed.redo(); // no-op, no crash
    }
}
