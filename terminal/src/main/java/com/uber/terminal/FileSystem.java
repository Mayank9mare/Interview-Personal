package com.uber.terminal;

import com.uber.terminal.model.Node;
import java.util.*;
import java.util.regex.*;

/**
 * In-memory file system supporting absolute and relative paths with {@code mkdir}, {@code cd},
 * {@code ls}, {@code pwd}, and regex-based {@code search}.
 * <p>
 * The tree is rooted at a synthetic "/" node whose parent points back to itself, making
 * {@code ".."} at the root a no-op. All operations work on {@link Node} objects; no disk I/O
 * is performed. Not thread-safe.
 */
public class FileSystem {
    /** The synthetic root node ("/"). */
    private final Node root;

    /** The node representing the current working directory. */
    private Node current;

    /**
     * Initialises the file system with a single root directory and sets the working directory to it.
     */
    public FileSystem() {
        root = new Node("/", false, null);
        root.parent = root;
        current = root;
    }

    /**
     * Returns the absolute path of the current working directory.
     *
     * @return absolute path string, e.g. {@code "/home/user"}
     */
    public String pwd() {
        return buildAbsPath(current);
    }

    /**
     * Resolves {@code path} to a {@link Node}, supporting absolute paths, relative paths,
     * {@code "."}, and {@code ".."}.
     *
     * @param path the path to resolve
     * @return the resolved {@code Node}, or {@code null} if any component does not exist
     */
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

    /**
     * Creates all missing directories along {@code path} (analogous to {@code mkdir -p}).
     * Existing nodes are left untouched.
     *
     * @param path absolute or relative path of the directory to create
     * @return empty string on success, or an error message if {@code path} is null/empty
     */
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

    /**
     * Changes the working directory to {@code path}.
     *
     * @param path absolute or relative path to the target directory
     * @return empty string on success, or an error message if the path does not exist or is a file
     */
    public String cd(String path) {
        if (path == null || path.isEmpty()) return "Invalid path";
        Node target = resolvePath(path);
        if (target == null) return "No such file or directory: " + path;
        if (target.isFile) return "Not a directory: " + path;
        current = target;
        return "";
    }

    /**
     * Lists the contents of a directory in alphabetical order, prefixed with {@code [FILE]} or
     * {@code [DIR]}. Uses the current directory when {@code path} is null or empty.
     *
     * @param path absolute or relative path to list, or {@code null} for the current directory
     * @return newline-separated listing, or an error message if the path does not exist
     */
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

    /**
     * Recursively searches the subtree rooted at the current directory for entries whose names
     * match the given regular expression (partial match via {@link java.util.regex.Matcher#find}).
     * Results are returned as sorted absolute paths.
     *
     * @param regex Java regular expression to match against entry names
     * @return newline-separated sorted absolute paths of matching entries, {@code "No matches found"},
     *         or {@code "Invalid regex: ..."} for a malformed pattern
     */
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

    /** Recursive DFS helper that collects matching absolute paths into {@code results}. */
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

    /** Walks parent pointers to construct an absolute path string for {@code node}. */
    private String buildAbsPath(Node node) {
        if (node == root) return "/";
        List<String> parts = new ArrayList<>();
        Node n = node;
        while (n != root) { parts.add(n.name); n = n.parent; }
        Collections.reverse(parts);
        return "/" + String.join("/", parts);
    }
}
