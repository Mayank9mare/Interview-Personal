// Companies: Uber, Atlassian
package com.uber.fswildcard;

import java.util.*;

public class FileSystemWildcard {
    private static class Node {
        final String name;
        Node parent;
        final TreeMap<String, Node> children = new TreeMap<>();
        Node(String name, Node parent) { this.name = name; this.parent = parent; }
    }

    private final Node root;
    private Node current;

    public FileSystemWildcard() {
        root = new Node("", null);
        root.parent = root;
        current = root;
    }

    public String pwd() {
        if (current == root) return "/";
        Deque<String> parts = new ArrayDeque<>();
        Node n = current;
        while (n != root) { parts.addFirst(n.name); n = n.parent; }
        return "/" + String.join("/", parts);
    }

    public void mkdir(String path) {
        Node base = path.startsWith("/") ? root : current;
        for (String seg : segments(path)) {
            if (seg.equals(".")) continue;
            if (seg.equals("..")) { base = base.parent; continue; }
            final Node parent = base;
            base = base.children.computeIfAbsent(seg, k -> new Node(k, parent));
        }
    }

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

    private List<String> segments(String path) {
        List<String> segs = new ArrayList<>();
        for (String s : path.split("/")) if (!s.isEmpty()) segs.add(s);
        return segs;
    }
}
