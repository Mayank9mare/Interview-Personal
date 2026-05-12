# Uber Terminal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an in-memory Unix-like terminal in Java supporting `mkdir`, `cd`, `ls`, `pwd`, and `search <regex>` with a REPL entry point.

**Architecture:** N-ary tree of `Node` objects (file or directory) with parent pointers. `FileSystem` owns root + current-dir pointer and exposes one `String`-returning method per command. `Terminal` drives the REPL and a hardcoded demo.

**Tech Stack:** Java 11, Maven 3, JUnit 5.10

---

## File Map

| File | Responsibility |
|---|---|
| `pom.xml` | Maven build, JUnit 5 dependency |
| `src/main/java/com/uber/terminal/model/Node.java` | Tree node (name, isFile, content, parent, children) |
| `src/main/java/com/uber/terminal/FileSystem.java` | Core API — all five commands |
| `src/main/java/com/uber/terminal/Terminal.java` | `main` — demo block + REPL |
| `src/test/java/com/uber/terminal/FileSystemTest.java` | Unit tests for all FileSystem commands |

---

### Task 1: Project Setup

**Files:**
- Create: `pom.xml`
- Create dirs: `src/main/java/com/uber/terminal/model/`, `src/test/java/com/uber/terminal/`

- [ ] **Step 1: Create `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.uber</groupId>
    <artifactId>terminal</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create directory structure**

```bash
mkdir -p src/main/java/com/uber/terminal/model
mkdir -p src/test/java/com/uber/terminal
```

- [ ] **Step 3: Verify Maven resolves dependencies**

Run: `mvn compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git init
git add pom.xml
git commit -m "chore: maven project setup"
```

---

### Task 2: Node Model

**Files:**
- Create: `src/main/java/com/uber/terminal/model/Node.java`

- [ ] **Step 1: Create `Node.java`**

```java
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
```

- [ ] **Step 2: Compile**

Run: `mvn compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/uber/terminal/model/Node.java
git commit -m "feat: add Node tree model"
```

---

### Task 3: FileSystem + `pwd`

**Files:**
- Create: `src/main/java/com/uber/terminal/FileSystem.java`
- Create: `src/test/java/com/uber/terminal/FileSystemTest.java`

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/com/uber/terminal/FileSystemTest.java`:

```java
package com.uber.terminal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemTest {
    private FileSystem fs;

    @BeforeEach
    void setUp() {
        fs = new FileSystem();
    }

    // --- pwd ---
    @Test
    void pwd_atRoot_returnsSlash() {
        assertEquals("/", fs.pwd());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test`
Expected: `COMPILATION ERROR` — `FileSystem` does not exist yet

- [ ] **Step 3: Create `FileSystem.java` with `pwd`**

```java
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
        if (current == root) return "/";
        List<String> parts = new ArrayList<>();
        Node node = current;
        while (node != root) {
            parts.add(node.name);
            node = node.parent;
        }
        Collections.reverse(parts);
        return "/" + String.join("/", parts);
    }

    // resolvePath and other commands added in later tasks
    private Node resolvePath(String path) {
        Node base = path.startsWith("/") ? root : current;
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) { base = base.parent; continue; }
            if (!base.children.containsKey(part)) return null;
            base = base.children.get(part);
        }
        return base;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: `Tests run: 1, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/uber/terminal/FileSystem.java \
        src/test/java/com/uber/terminal/FileSystemTest.java
git commit -m "feat: add FileSystem with pwd"
```

---

### Task 4: `mkdir`

**Files:**
- Modify: `src/main/java/com/uber/terminal/FileSystem.java`
- Modify: `src/test/java/com/uber/terminal/FileSystemTest.java`

- [ ] **Step 1: Add failing tests** (add to `FileSystemTest.java` inside the class, after existing tests)

```java
    // --- mkdir ---
    @Test
    void mkdir_absolutePath_createsDir() {
        assertEquals("", fs.mkdir("/home"));
        assertNotNull(fs.resolvePath("/home"));
    }

    @Test
    void mkdir_nestedPath_createsIntermediateDirs() {
        fs.mkdir("/a/b/c");
        assertNotNull(fs.resolvePath("/a/b/c"));
        assertNotNull(fs.resolvePath("/a/b"));
        assertNotNull(fs.resolvePath("/a"));
    }

    @Test
    void mkdir_duplicate_isNoOp() {
        fs.mkdir("/home");
        assertEquals("", fs.mkdir("/home"));
        assertEquals(1, fs.resolvePath("/").children.size());
    }

    @Test
    void mkdir_emptyPath_returnsError() {
        String result = fs.mkdir("");
        assertEquals("Invalid path", result);
    }

    @Test
    void mkdir_relativePath_createsFromCurrent() {
        fs.mkdir("/home");
        fs.cd("/home");
        fs.mkdir("user");
        assertNotNull(fs.resolvePath("/home/user"));
    }
```

Note: `resolvePath` must be made package-private (remove `private`) for test access.

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test`
Expected: compilation errors — `mkdir` and `cd` not defined, `resolvePath` private

- [ ] **Step 3: Implement `mkdir` and make `resolvePath` package-private**

Replace `FileSystem.java` with:

```java
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

    public String mkdir(String path) {
        if (path == null || path.isEmpty()) return "Invalid path";
        Node base = path.startsWith("/") ? root : current;
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) { base = base.parent; continue; }
            base.children.computeIfAbsent(part, k -> new Node(k, false, base));
            base = base.children.get(part);
        }
        return "";
    }

    public String pwd() {
        if (current == root) return "/";
        List<String> parts = new ArrayList<>();
        Node node = current;
        while (node != root) {
            parts.add(node.name);
            node = node.parent;
        }
        Collections.reverse(parts);
        return "/" + String.join("/", parts);
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
}
```

Note: `cd` is tested here but not yet implemented — add a temporary `cd` stub returning `""` at bottom of class so the test file compiles:

```java
    public String cd(String path) { return ""; }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test`
Expected: `Tests run: 6, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/uber/terminal/FileSystem.java \
        src/test/java/com/uber/terminal/FileSystemTest.java
git commit -m "feat: implement mkdir"
```

---

### Task 5: `ls`

**Files:**
- Modify: `src/main/java/com/uber/terminal/FileSystem.java`
- Modify: `src/test/java/com/uber/terminal/FileSystemTest.java`

- [ ] **Step 1: Add failing tests** (add inside `FileSystemTest` class)

```java
    // --- ls ---
    @Test
    void ls_emptyDir_returnsEmptyString() {
        assertEquals("", fs.ls(null));
    }

    @Test
    void ls_multipleChildren_sortedAlphabetically() {
        fs.mkdir("/z");
        fs.mkdir("/a");
        fs.mkdir("/m");
        assertEquals("[DIR] a\n[DIR] m\n[DIR] z", fs.ls("/"));
    }

    @Test
    void ls_specificPath_listsCorrectDir() {
        fs.mkdir("/home/user");
        assertEquals("[DIR] user", fs.ls("/home"));
    }

    @Test
    void ls_nonExistentPath_returnsError() {
        assertTrue(fs.ls("/nope").startsWith("No such file or directory"));
    }

    @Test
    void ls_noArg_defaultsToCurrentDir() {
        fs.mkdir("/home");
        fs.cd("/home");
        fs.mkdir("docs");
        assertEquals("[DIR] docs", fs.ls(null));
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test`
Expected: compilation error — `ls` not defined

- [ ] **Step 3: Implement `ls`** (add to `FileSystem.java`, replace the `cd` stub too)

```java
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

    public String cd(String path) { return ""; } // stub — replaced in Task 6
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test`
Expected: `Tests run: 11, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/uber/terminal/FileSystem.java \
        src/test/java/com/uber/terminal/FileSystemTest.java
git commit -m "feat: implement ls"
```

---

### Task 6: `cd`

**Files:**
- Modify: `src/main/java/com/uber/terminal/FileSystem.java`
- Modify: `src/test/java/com/uber/terminal/FileSystemTest.java`

- [ ] **Step 1: Add failing tests**

```java
    // --- cd ---
    @Test
    void cd_absolutePath_changesCurrent() {
        fs.mkdir("/home/user");
        assertEquals("", fs.cd("/home/user"));
        assertEquals("/home/user", fs.pwd());
    }

    @Test
    void cd_relativePath_changesCurrent() {
        fs.mkdir("/home");
        fs.cd("/home");
        fs.mkdir("user");
        fs.cd("user");
        assertEquals("/home/user", fs.pwd());
    }

    @Test
    void cd_dotDot_goesUpOneLevel() {
        fs.mkdir("/home/user");
        fs.cd("/home/user");
        fs.cd("..");
        assertEquals("/home", fs.pwd());
    }

    @Test
    void cd_dotDotAtRoot_staysAtRoot() {
        fs.cd("..");
        assertEquals("/", fs.pwd());
    }

    @Test
    void cd_nonExistentPath_returnsError() {
        assertTrue(fs.cd("/nope").startsWith("No such file or directory"));
    }

    @Test
    void cd_slash_returnsToRoot() {
        fs.mkdir("/home");
        fs.cd("/home");
        fs.cd("/");
        assertEquals("/", fs.pwd());
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test`
Expected: 3–4 failures — stub `cd` always returns `""` but does not change `current`

- [ ] **Step 3: Replace the `cd` stub with real implementation**

In `FileSystem.java`, replace `public String cd(String path) { return ""; }` with:

```java
    public String cd(String path) {
        if (path == null || path.isEmpty()) return "Invalid path";
        Node target = resolvePath(path);
        if (target == null) return "No such file or directory: " + path;
        if (target.isFile) return "Not a directory: " + path;
        current = target;
        return "";
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test`
Expected: `Tests run: 17, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/uber/terminal/FileSystem.java \
        src/test/java/com/uber/terminal/FileSystemTest.java
git commit -m "feat: implement cd"
```

---

### Task 7: `search`

**Files:**
- Modify: `src/main/java/com/uber/terminal/FileSystem.java`
- Modify: `src/test/java/com/uber/terminal/FileSystemTest.java`

- [ ] **Step 1: Add failing tests**

```java
    // --- search ---
    @Test
    void search_exactName_returnsAbsolutePath() {
        fs.mkdir("/logs/app");
        String result = fs.search("app");
        assertTrue(result.contains("/logs/app"));
    }

    @Test
    void search_regexPattern_matchesMultiple() {
        fs.mkdir("/logs");
        fs.mkdir("/logs2");
        fs.mkdir("/other");
        String result = fs.search("logs.*");
        assertTrue(result.contains("/logs"));
        assertTrue(result.contains("/logs2"));
        assertFalse(result.contains("/other"));
    }

    @Test
    void search_noMatch_returnsNoMatchesMessage() {
        assertEquals("No matches found", fs.search("xyz"));
    }

    @Test
    void search_invalidRegex_returnsError() {
        assertTrue(fs.search("[invalid").startsWith("Invalid regex"));
    }

    @Test
    void search_fromCurrentDir_scopedToSubtree() {
        fs.mkdir("/a/target");
        fs.mkdir("/b/other");
        fs.cd("/a");
        String result = fs.search("target");
        assertTrue(result.contains("/a/target"));
        assertFalse(result.contains("/b"));
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test`
Expected: compilation error — `search` not defined

- [ ] **Step 3: Implement `search`** (add to `FileSystem.java`)

```java
    public String search(String regex) {
        try {
            Pattern pattern = Pattern.compile(regex);
            List<String> results = new ArrayList<>();
            dfs(current, pattern, results, buildAbsPath(current));
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
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test`
Expected: `Tests run: 22, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/uber/terminal/FileSystem.java \
        src/test/java/com/uber/terminal/FileSystemTest.java
git commit -m "feat: implement search with regex"
```

---

### Task 8: Terminal REPL + Demo

**Files:**
- Create: `src/main/java/com/uber/terminal/Terminal.java`

- [ ] **Step 1: Create `Terminal.java`**

```java
package com.uber.terminal;

import java.util.Scanner;

public class Terminal {

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();

        System.out.println("=== Demo ===");
        run(fs, "mkdir /home/user/docs");
        run(fs, "mkdir /home/user/downloads");
        run(fs, "mkdir /var/log/app");
        run(fs, "ls /");
        run(fs, "cd /home/user");
        run(fs, "pwd");
        run(fs, "ls");
        run(fs, "search user");
        run(fs, "cd /");
        run(fs, "search log.*");
        run(fs, "cd /nope");
        run(fs, "search [bad");

        System.out.println("\n=== Terminal (type 'exit' to quit) ===");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(fs.pwd() + " $ ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.equals("exit")) break;
            if (line.isEmpty()) continue;
            String result = dispatch(fs, line);
            if (!result.isEmpty()) System.out.println(result);
        }
        scanner.close();
    }

    static String dispatch(FileSystem fs, String line) {
        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0];
        String arg = parts.length > 1 ? parts[1] : null;
        switch (cmd) {
            case "mkdir":  return arg != null ? fs.mkdir(arg)  : "Usage: mkdir <path>";
            case "cd":     return arg != null ? fs.cd(arg)     : "Usage: cd <path>";
            case "ls":     return fs.ls(arg);
            case "pwd":    return fs.pwd();
            case "search": return arg != null ? fs.search(arg) : "Usage: search <regex>";
            default:       return "Unknown command: " + cmd;
        }
    }

    private static void run(FileSystem fs, String cmd) {
        System.out.println(fs.pwd() + " $ " + cmd);
        String result = dispatch(fs, cmd);
        if (!result.isEmpty()) System.out.println(result);
    }
}
```

- [ ] **Step 2: Compile**

Run: `mvn compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Run the demo**

Run: `mvn compile && java -cp target/classes com.uber.terminal.Terminal`

Expected output (demo section, then interactive prompt):
```
=== Demo ===
/ $ mkdir /home/user/docs
/ $ mkdir /home/user/downloads
/ $ mkdir /var/log/app
/ $ ls /
[DIR] home
[DIR] var
/ $ cd /home/user
/ $ pwd
/home/user
/home/user $ ls
[DIR] docs
[DIR] downloads
/home/user $ search user
No matches found
/home/user $ cd /
/ $ search log.*
/var/log
/ $ cd /nope
No such file or directory: /nope
/ $ search [bad
Invalid regex: [bad

=== Terminal (type 'exit' to quit) ===
/ $
```

Note: `search user` returns no matches because `search` checks children of the current node — "user" is a child of "/home", and current is "/home/user", so "user" is not in the subtree of "/home/user". This is correct per spec.

- [ ] **Step 4: Run full test suite**

Run: `mvn test`
Expected: `Tests run: 22, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/uber/terminal/Terminal.java
git commit -m "feat: add Terminal REPL and demo"
```

---

## Self-Review

**Spec coverage:**
- `mkdir` (absolute, relative, nested, no-op, error) ✓ Tasks 4
- `cd` (absolute, relative, .., root.., error, file error) ✓ Task 6
- `ls` (default, path, sorted, error) ✓ Task 5
- `pwd` (root, nested) ✓ Task 3
- `search` (match, no match, regex, invalid regex, scoped to subtree) ✓ Task 7
- REPL + demo ✓ Task 8

**Placeholder scan:** No TBDs or incomplete steps.

**Type consistency:**
- `resolvePath(String) → Node` used in Tasks 4, 5, 6 — consistent ✓
- `buildAbsPath(Node) → String` introduced and used only in Task 7 ✓
- `dfs(Node, Pattern, List<String>, String)` introduced and used only in Task 7 ✓
- All public methods return `String` — consistent ✓
