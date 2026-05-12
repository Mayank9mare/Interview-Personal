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
}
