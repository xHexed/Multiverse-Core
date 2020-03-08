package com.onarandombox.MultiverseCore.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dumptruckman.minecraft.util.Logging;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileUtilsTest {

    private Path tempDir;
    private Path parentDir;
    private Path parentDirFile;
    private Path childDir;
    private Path childDirFile;


    @Before
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("testingTempDir");

        parentDir = Files.createDirectory(tempDir.resolve("parentDir"));
        parentDirFile = Files.createFile(parentDir.resolve("parentDirFile.txt"));

        childDir = Files.createDirectory(parentDir.resolve("childDir"));
        childDirFile = Files.createFile(childDir.resolve("childDirFile.txt"));

        assertTrue(Files.isDirectory(parentDir));
        assertTrue(Files.isRegularFile(parentDirFile));
        assertTrue(Files.isDirectory(childDir));
        assertTrue(Files.isRegularFile(childDirFile));
    }

    @After
    public void tearDown() throws Exception {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(tempDir.toFile());
        }
        catch (final IOException e) {
            if (Files.exists(tempDir)) {
                throw e;
            }
        }
    }

    @Test
    public void deleteFolder() {
        FileUtils.deleteFolder(parentDir.toFile());
        assertFalse(Files.isDirectory(parentDir));
        assertFalse(Files.isRegularFile(parentDirFile));
        assertFalse(Files.isDirectory(childDir));
        assertFalse(Files.isRegularFile(childDirFile));
    }

    @Test
    public void deleteFolderContents() {
        FileUtils.deleteFolderContents(parentDir.toFile());
        assertTrue(Files.isDirectory(parentDir));
        assertFalse(Files.isRegularFile(parentDirFile));
        assertFalse(Files.isDirectory(childDir));
        assertFalse(Files.isRegularFile(childDirFile));
    }

    @Test
    public void copyFolder() {
        final Path targetDir = tempDir.resolve("target");
        final Path targetFile = targetDir.resolve("parentDirFile.txt");
        final Path targetChildDir = targetDir.resolve("childDir");
        final Path targetChildDirFile = targetChildDir.resolve("childDirFile.txt");

        assertFalse(Files.isDirectory(targetDir));
        assertFalse(Files.isRegularFile(targetFile));
        assertFalse(Files.isDirectory(targetChildDir));
        assertFalse(Files.isRegularFile(targetChildDirFile));

        assertTrue(FileUtils.copyFolder(parentDir.toFile(), targetDir.toFile(), Logging.getLogger()));

        assertTrue(Files.isDirectory(targetDir));
        assertTrue(Files.isRegularFile(targetFile));
        assertTrue(Files.isDirectory(targetChildDir));
        assertTrue(Files.isRegularFile(targetChildDirFile));
    }

    @Test
    public void copyFolder_intoExistingFolder() throws Exception {
        final Path targetDir = Files.createDirectory(tempDir.resolve("target"));
        final Path targetFile = targetDir.resolve("parentDirFile.txt");
        final Path targetChildDir = targetDir.resolve("childDir");
        final Path targetChildDirFile = targetChildDir.resolve("childDirFile.txt");

        assertTrue(Files.isDirectory(targetDir));
        assertFalse(Files.isRegularFile(targetFile));
        assertFalse(Files.isDirectory(targetChildDir));
        assertFalse(Files.isRegularFile(targetChildDirFile));

        assertTrue(FileUtils.copyFolder(parentDir.toFile(), targetDir.toFile(), Logging.getLogger()));

        assertTrue(Files.isDirectory(targetDir));
        assertTrue(Files.isRegularFile(targetFile));
        assertTrue(Files.isDirectory(targetChildDir));
        assertTrue(Files.isRegularFile(targetChildDirFile));
    }

    @Test
    public void copyFolder_intoExistingFolder_whereFileExists() throws Exception {
        final Path targetDir = Files.createDirectory(tempDir.resolve("target"));
        final Path targetFile = Files.createFile(targetDir.resolve("parentDirFile.txt"));

        assertTrue(Files.isDirectory(targetDir));
        assertTrue(Files.isRegularFile(targetFile));

        assertFalse(FileUtils.copyFolder(parentDir.toFile(), targetDir.toFile(), Logging.getLogger()));
    }
}
