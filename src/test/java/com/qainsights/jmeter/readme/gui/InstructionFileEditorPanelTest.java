package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.instructions.DetectedInstructionFile;
import com.qainsights.jmeter.readme.instructions.InstructionFileKind;
import com.qainsights.jmeter.readme.instructions.InstructionFileSource;
import com.qainsights.jmeter.readme.instructions.InstructionFileStore;
import com.qainsights.jmeter.readme.instructions.LoadedInstructionFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructionFileEditorPanelTest {

    @TempDir
    Path tempDir;

    @Test
    void editsAndSavesTheBackingFile() throws Exception {
        Path path = Files.writeString(tempDir.resolve("CLAUDE.md"), "original");
        InstructionFileStore store = new InstructionFileStore();
        LoadedInstructionFile loaded = store.load(new DetectedInstructionFile(path, InstructionFileKind.CLAUDE,
                InstructionFileSource.TEST_PLAN_DIRECTORY));
        InstructionFileEditorPanel[] holder = new InstructionFileEditorPanel[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new InstructionFileEditorPanel(loaded, store));

        SwingUtilities.invokeAndWait(() -> holder[0].setEditorText("updated"));
        assertTrue(holder[0].hasUnsavedChanges());
        SwingUtilities.invokeAndWait(() -> holder[0].saveChanges());

        assertEquals("updated", Files.readString(path));
        assertFalse(holder[0].hasUnsavedChanges());
    }

    @Test
    void togglesWordWrappingWithoutChangingContent() throws Exception {
        Path path = Files.writeString(tempDir.resolve("CLAUDE.md"), "a long instruction line");
        InstructionFileStore store = new InstructionFileStore();
        LoadedInstructionFile loaded = store.load(new DetectedInstructionFile(path, InstructionFileKind.CLAUDE,
                InstructionFileSource.TEST_PLAN_DIRECTORY));
        InstructionFileEditorPanel[] holder = new InstructionFileEditorPanel[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new InstructionFileEditorPanel(loaded, store));

        assertFalse(holder[0].isWordWrapEnabled());
        SwingUtilities.invokeAndWait(() -> holder[0].setWordWrap(true));

        assertTrue(holder[0].isWordWrapEnabled());
        assertEquals("a long instruction line", holder[0].getEditorText());
        assertFalse(holder[0].hasUnsavedChanges());
    }

    @Test
    void keepsOnlySaveAsAVisibleFileAction() throws Exception {
        Path path = Files.writeString(tempDir.resolve("AGENTS.md"), "rules");
        InstructionFileStore store = new InstructionFileStore();
        LoadedInstructionFile loaded = store.load(new DetectedInstructionFile(path, InstructionFileKind.AGENTS,
                InstructionFileSource.GIT_ROOT));
        InstructionFileEditorPanel[] holder = new InstructionFileEditorPanel[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new InstructionFileEditorPanel(loaded, store));

        assertNotNull(findButton(holder[0], "Save"));
        assertNotNull(findButton(holder[0], "⋮"));
        assertNull(findButton(holder[0], "Reload"));
        assertNull(findButton(holder[0], "Revert"));
    }

    @Test
    void reloadsExternalChangesAndRevertsEditorChanges() throws Exception {
        Path path = Files.writeString(tempDir.resolve("AGENTS.md"), "first");
        InstructionFileStore store = new InstructionFileStore();
        LoadedInstructionFile loaded = store.load(new DetectedInstructionFile(path, InstructionFileKind.AGENTS,
                InstructionFileSource.GIT_ROOT));
        InstructionFileEditorPanel[] holder = new InstructionFileEditorPanel[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new InstructionFileEditorPanel(loaded, store));

        SwingUtilities.invokeAndWait(() -> holder[0].setEditorText("local change"));
        SwingUtilities.invokeAndWait(() -> holder[0].revertChanges());
        assertEquals("first", holder[0].getEditorText());
        Files.writeString(path, "external change");
        SwingUtilities.invokeAndWait(() -> holder[0].reloadFromDisk());

        assertEquals("external change", holder[0].getEditorText());
        assertFalse(holder[0].hasUnsavedChanges());
    }

    private static JButton findButton(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button && text.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container child) {
                JButton result = findButton(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
