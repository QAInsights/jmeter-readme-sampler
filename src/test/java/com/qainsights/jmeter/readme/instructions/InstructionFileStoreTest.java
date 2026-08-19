package com.qainsights.jmeter.readme.instructions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructionFileStoreTest {

    @TempDir
    Path tempDir;

    private final InstructionFileStore store = new InstructionFileStore();

    @Test
    void loadsUtf8ContentAndMetadata() throws IOException {
        Path path = tempDir.resolve("CLAUDE.md");
        Files.writeString(path, "# 你好\n\nInstructions\n", StandardCharsets.UTF_8);
        DetectedInstructionFile file = new DetectedInstructionFile(path, InstructionFileKind.CLAUDE,
                InstructionFileSource.TEST_PLAN_DIRECTORY);

        LoadedInstructionFile loaded = store.load(file);

        assertEquals("# 你好\n\nInstructions\n", loaded.content());
        assertEquals("\n", loaded.lineSeparator());
        assertTrue(loaded.writable());
    }

    @Test
    void preservesBomAndCrLfWhenSaving() throws IOException {
        Path path = tempDir.resolve("AGENTS.md");
        byte[] original = "# Rules\r\n\r\nFirst\r\n".getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[original.length + 3];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(original, 0, withBom, 3, original.length);
        Files.write(path, withBom);
        LoadedInstructionFile loaded = store.load(new DetectedInstructionFile(path, InstructionFileKind.AGENTS,
                InstructionFileSource.TEST_PLAN_DIRECTORY));

        LoadedInstructionFile saved = store.save(loaded, "# Rules\n\nUpdated\n");

        byte[] expectedBody = "# Rules\r\n\r\nUpdated\r\n".getBytes(StandardCharsets.UTF_8);
        byte[] expected = new byte[expectedBody.length + 3];
        expected[0] = (byte) 0xEF;
        expected[1] = (byte) 0xBB;
        expected[2] = (byte) 0xBF;
        System.arraycopy(expectedBody, 0, expected, 3, expectedBody.length);
        assertArrayEquals(expected, Files.readAllBytes(path));
        assertEquals("# Rules\r\n\r\nUpdated\r\n", saved.content());
    }

    @Test
    void refusesToOverwriteExternalChanges() throws IOException {
        Path path = tempDir.resolve("CLAUDE.md");
        Files.writeString(path, "original");
        LoadedInstructionFile loaded = store.load(new DetectedInstructionFile(path, InstructionFileKind.CLAUDE,
                InstructionFileSource.TEST_PLAN_DIRECTORY));
        Files.writeString(path, "external change");

        assertThrows(InstructionFileChangedException.class, () -> store.save(loaded, "editor change"));
        assertEquals("external change", Files.readString(path));
    }

    @Test
    void createsMissingFileWithoutOverwritingExistingFile() throws IOException {
        Path path = tempDir.resolve("AGENTS.md");

        LoadedInstructionFile created = store.create(path, InstructionFileKind.AGENTS,
                InstructionFileSource.TEST_PLAN_DIRECTORY);

        assertTrue(Files.exists(path));
        assertEquals("", created.content());
        assertThrows(FileAlreadyExistsException.class, () -> store.create(path, InstructionFileKind.AGENTS,
                InstructionFileSource.TEST_PLAN_DIRECTORY));
    }
}
