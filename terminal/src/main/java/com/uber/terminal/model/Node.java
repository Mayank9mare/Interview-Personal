package com.uber.terminal.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A single node in an in-memory file-system tree, representing either a file or a directory.
 * <p>
 * Directories hold named child nodes in a {@code HashMap}; files carry an (empty) content field.
 * The root node points to itself as its own parent to simplify {@code ".."} traversal.
 * Not thread-safe.
 */
public class Node {
    /** Entry name (not the full path). */
    public final String name;

    /** {@code true} if this node is a file; {@code false} if it is a directory. */
    public final boolean isFile;

    /** File content. Always empty in this implementation (content is not persisted). */
    public final String content;

    /** Parent node; the root node points to itself. */
    public Node parent;

    /** Named children; empty for file nodes. */
    public final Map<String, Node> children;

    /**
     * @param name   entry name (not the full path)
     * @param isFile {@code true} for a file, {@code false} for a directory
     * @param parent parent node ({@code null} only transiently before the root wires itself)
     */
    public Node(String name, boolean isFile, Node parent) {
        this.name = name;
        this.isFile = isFile;
        this.content = "";
        this.parent = parent;
        this.children = new HashMap<>();
    }
}
