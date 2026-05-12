package com.uber.fswildcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSystemWildcardTest {
    private FileSystemWildcard fs;

    @BeforeEach void setUp() { fs = new FileSystemWildcard(); }

    @Test
    void pwd_rootInitially() { assertEquals("/", fs.pwd()); }

    @Test
    void mkdir_and_pwd() {
        fs.mkdir("/a/b/c");
        fs.cd("a/b");
        assertEquals("/a/b", fs.pwd());
    }

    @Test
    void cd_wildcardPicksLexSmallestChild() {
        fs.mkdir("/a/b/c");
        fs.mkdir("/a/b/apple");
        fs.cd("/a/b");
        fs.cd("*"); // children: apple, c → picks "apple"
        assertEquals("/a/b/apple", fs.pwd());
    }

    @Test
    void cd_wildcardNoChildren_staysAtCurrent() {
        fs.mkdir("/a/b/c");
        fs.cd("/a/b/c");
        fs.cd("*"); // no children → stay
        assertEquals("/a/b/c", fs.pwd());
    }

    @Test
    void cd_wildcardThroughPath() {
        fs.mkdir("/a/b/c");
        fs.cd("/a/b");
        fs.cd("*"); // picks c
        assertEquals("/a/b/c", fs.pwd());
    }

    @Test
    void cd_wildcardAbsolutePath() {
        fs.mkdir("/a/b");
        fs.cd("/*"); // at root, picks "a"
        assertEquals("/a", fs.pwd());
    }

    @Test
    void cd_failsOnMissingSegment_leavesCurrentUnchanged() {
        fs.mkdir("/a/b");
        fs.cd("/a/b");
        fs.cd("/nope/x"); // /nope doesn't exist → fail
        assertEquals("/a/b", fs.pwd()); // unchanged
    }

    @Test
    void cd_dotDot() {
        fs.mkdir("/a/b/c");
        fs.cd("/a/b/c");
        fs.cd("..");
        assertEquals("/a/b", fs.pwd());
    }

    @Test
    void cd_wildcardWithDotDot() {
        fs.mkdir("/a/b/c");
        fs.cd("/a/b/c");
        fs.cd("../*"); // .. → /a/b, * picks c
        assertEquals("/a/b/c", fs.pwd());
    }

    @Test
    void mkdir_idempotent() {
        fs.mkdir("/a/b");
        fs.mkdir("/a/b"); // no-op
        fs.cd("/a/b");
        assertEquals("/a/b", fs.pwd());
    }
}
