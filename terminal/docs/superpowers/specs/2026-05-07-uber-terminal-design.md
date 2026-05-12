# Uber LLD: In-Memory Terminal / File System

**Date:** 2026-05-07  
**Language:** Java  
**Scope:** Standard Uber SDE-2 machine coding question

---

## Problem Statement

Design and implement an in-memory terminal application that supports core Unix file system commands. The system maintains a tree-based virtual file system in memory and exposes a REPL interface.

---

## Commands

| Command | Syntax | Behavior |
|---|---|---|
| mkdir | `mkdir <path>` | Creates directory at path; creates all intermediate directories (like `mkdir -p`); no-op if already exists |
| cd | `cd <path>` | Changes current directory; supports `.`, `..`, absolute and relative paths |
| ls | `ls [path]` | Lists children of directory sorted alphabetically; prefixes `[DIR]` or `[FILE]`; defaults to current dir |
| pwd | `pwd` | Prints absolute path of current working directory |
| search | `search <regex>` | DFS from current directory; returns full paths of all nodes whose name matches the Java regex |

---

## Architecture

```
terminal/
└── src/main/java/com/uber/terminal/
    ├── model/
    │   └── Node.java        # tree node (file or directory)
    ├── FileSystem.java      # core API
    └── Terminal.java        # main — REPL + demo
```

---

## Data Model

### Node.java
```
String name
boolean isFile
String content          // empty for directories
Node parent             // null for root
Map<String, Node> children   // HashMap; sorted on demand for ls
```

### FileSystem.java state
```
Node root     // the "/" node
Node current  // current working directory pointer
```

---

## Path Resolution

- Absolute path (starts with `/`) → resolve from `root`
- Relative path → resolve from `current`
- Segment `.` → current node (skip)
- Segment `..` → parent node; root's parent is root itself
- Segments split on `/`, empty segments ignored

---

## Command Behavior Details

### mkdir
1. Resolve base (root if absolute, current if relative)
2. Walk each segment; create `Node(isFile=false)` if missing
3. No-op if node already exists
4. Return empty string on success

### cd
1. Resolve full path using path resolution rules
2. If node not found → error
3. If node is a file → error
4. Set `current = resolvedNode`

### ls
1. Resolve path (default: current)
2. If node not found → error
3. If node is a file → return `[FILE] <name>`
4. If directory → collect children names, sort, prefix each with `[DIR]` or `[FILE]`

### pwd
1. Walk from `current` up to root via `parent` links, collecting names
2. Reverse and join with `/`
3. Root returns `/`

### search
1. Compile `Pattern` from regex; catch `PatternSyntaxException`
2. DFS from `current`
3. At each node, check if `name` matches pattern
4. Collect and return matching full paths (absolute from root)

---

## Error Handling

| Condition | Message |
|---|---|
| Path not found (cd/ls) | `No such file or directory: <path>` |
| cd into a file | `Not a directory: <path>` |
| Invalid regex | `Invalid regex: <pattern>` |
| Empty/null path | `Invalid path` |
| Unknown command | `Unknown command: <cmd>` |

- All errors returned as strings (no exceptions exposed from FileSystem)
- Root `..` is a no-op (stays at root)

---

## Terminal (Main)

Two modes run in sequence:

1. **Demo block** — hardcoded sequence of commands exercising all five commands, printed with `> command` style output
2. **REPL** — reads from stdin, dispatches to FileSystem, prints result, loops until `exit`

---

## Design Decisions

- **HashMap over TreeMap** — O(1) child lookup; sort only on `ls` calls (O(k log k) where k = children count)
- **Parent pointer on Node** — makes `pwd` O(depth) without extra state in FileSystem
- **String return type** — all FileSystem methods return String; empty string = success, non-empty = message/error; keeps Terminal simple
- **No checked exceptions** — all errors are encoded in the return string; FileSystem never throws
