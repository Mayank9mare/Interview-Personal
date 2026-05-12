package com.uber.terminal.model;

import java.util.HashMap;
import java.util.Map;

public class Node {
    public final String name;
    public final boolean isFile;
    public final String content;
    public Node parent;
    public final Map<String, Node> children;

    public Node(String name, boolean isFile, Node parent) {
        this.name = name;
        this.isFile = isFile;
        this.content = "";
        this.parent = parent;
        this.children = new HashMap<>();
    }
}
