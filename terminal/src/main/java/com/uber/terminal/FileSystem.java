package com.uber.terminal;

import com.uber.terminal.model.Node;
import java.util.*;
import java.util.regex.*;

public class FileSystem {
    private final Node root;
    private Node current;

    public FileSystem() {
        root = new Node("/", false, null);
        root.parent = root;
        current = root;
    }

    public String pwd() {
        return buildAbsPath(current);
    }

    // package-private for tests
    Node resolvePath(String path) {
        Node base = path.startsWith("/") ? root : current;
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) { base = base.parent; continue; }
            if (!base.children.containsKey(part)) return null;
            base = base.children.get(part);
        }
        return base;
    }

    public String mkdir(String path) {
        if (path == null || path.isEmpty()) return "Invalid path";
        Node base = path.startsWith("/") ? root : current;
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) { base = base.parent; continue; }
            final Node parent = base;
            base.children.computeIfAbsent(part, k -> new Node(k, false, parent));
            base = base.children.get(part);
        }
        return "";
    }

    public String cd(String path) {
        if (path == null || path.isEmpty()) return "Invalid path";
        Node target = resolvePath(path);
        if (target == null) return "No such file or directory: " + path;
        if (target.isFile) return "Not a directory: " + path;
        current = target;
        return "";
    }

    public String ls(String path) {
        Node target = (path == null || path.isEmpty()) ? current : resolvePath(path);
        if (target == null) return "No such file or directory: " + path;
        if (target.isFile) return "[FILE] " + target.name;
        List<String> names = new ArrayList<>(target.children.keySet());
        Collections.sort(names);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            Node child = target.children.get(names.get(i));
            if (i > 0) sb.append("\n");
            sb.append(child.isFile ? "[FILE] " : "[DIR] ").append(child.name);
        }
        return sb.toString();
    }

    public String search(String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            List<String> results = new ArrayList<>();
            dfs(current, pattern, results, buildAbsPath(current));
            Collections.sort(results);
            return results.isEmpty() ? "No matches found" : String.join("\n", results);
        } catch (PatternSyntaxException e) {
            return "Invalid regex: " + regex;
        }
    }

    private void dfs(Node node, Pattern pattern, List<String> results, String nodePath) {
        for (Node child : node.children.values()) {
            String childPath = nodePath.equals("/") ? "/" + child.name : nodePath + "/" + child.name;
            if (pattern.matcher(child.name).find()) {
                results.add(childPath);
            }
            if (!child.isFile) {
                dfs(child, pattern, results, childPath);
            }
        }
    }

    private String buildAbsPath(Node node) {
        if (node == root) return "/";
        List<String> parts = new ArrayList<>();
        Node n = node;
        while (n != root) { parts.add(n.name); n = n.parent; }
        Collections.reverse(parts);
        return "/" + String.join("/", parts);
    }
}
