// Companies: Uber, Atlassian
package com.uber.fswildcard;

import java.util.*;

/**
 * In-memory hierarchical file system that supports wildcard {@code *} in paths.
 *
 * <p>Supported path semantics: absolute paths start with {@code /}; relative paths
 * are resolved from the current directory. {@code .} stays, {@code ..} goes up,
 * and {@code *} picks the lexicographically smallest child (no-op if no children).
 *
 * <p>Data structure: each {@link Node} holds a {@link TreeMap} of children keyed by
 * name, which provides O(log k) child lookup and O(1) lexicographic-minimum access
 * via {@link TreeMap#firstEntry()}.
 *
 * <p>Not thread-safe.
 */
public class FileSystemWildcard {
    /** A single directory node in the tree. */
    private static class Node {
        final String name;
        Node parent;
        /** Children sorted by name for deterministic wildcard resolution. */
        final TreeMap<String, Node> children = new TreeMap<>();
        Node(String name, Node parent) { this.name = name; this.parent = parent; }
    }

    /** The virtual root directory (empty name). */
    private final Node root;
    /** Working directory; changes with {@link #cd}. */
    private Node current;

    /** Initialises the file system with only the root directory as the working directory. */
    public FileSystemWildcard() {
        root = new Node("", null);
        root.parent = root;
        current = root;
    }

    /**
     * @return absolute path of the current working directory (e.g. {@code "/"} or {@code "/a/b"})
     */
    public String pwd() {
        if (current == root) return "/";
        Deque<String> parts = new ArrayDeque<>();
        Node n = current;
        while (n != root) { parts.addFirst(n.name); n = n.parent; }
        return "/" + String.join("/", parts);
    }

    /**
     * Creates all directories along the given path, including intermediate ones.
     * Existing directories are not affected. {@code .} and {@code ..} are honoured.
     *
     * @param path absolute or relative path of the directory to create
     */
    public void mkdir(String path) {
        Node base = path.startsWith("/") ? root : current;
        for (String seg : segments(path)) {
            if (seg.equals(".")) continue;
            if (seg.equals("..")) { base = base.parent; continue; }
            final Node parent = base;
            base = base.children.computeIfAbsent(seg, k -> new Node(k, parent));
        }
    }

    /**
     * Changes the working directory to the given path.
     *
     * <p>{@code *} resolves to the lexicographically smallest child; if the current
     * node has no children it acts like {@code .}. A non-existent literal segment
     * leaves the working directory unchanged.
     *
     * @param path absolute or relative target path (may contain {@code *})
     */
    public void cd(String path) {
        Node base = path.startsWith("/") ? root : current;
        for (String seg : segments(path)) {
            if (seg.equals(".")) continue;
            else if (seg.equals("..")) { base = base.parent; }
            else if (seg.equals("*")) {
                // pick lex smallest child; if no children, stay (.)
                if (!base.children.isEmpty()) {
                    base = base.children.firstEntry().getValue();
                }
                // else: stay at base (. semantics)
            } else {
                Node next = base.children.get(seg);
                if (next == null) return; // fail — leave current unchanged
                base = next;
            }
        }
        current = base;
    }

    /** Splits a path into non-empty segments, discarding leading/trailing slashes. */
    private List<String> segments(String path) {
        List<String> segs = new ArrayList<>();
        for (String s : path.split("/")) if (!s.isEmpty()) segs.add(s);
        return segs;
    }
}
