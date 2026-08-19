package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.instructions.InstructionFileKind;
import com.qainsights.jmeter.readme.instructions.InstructionFileLocator;
import com.qainsights.jmeter.readme.instructions.InstructionFileStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructionFilesPanelTest {

    @TempDir
    Path tempDir;

    @Test
    void showsEveryDetectedFileWithItsSource() throws Exception {
        Path project = Files.createDirectories(tempDir.resolve("project"));
        Files.createDirectory(project.resolve(".git"));
        Path plans = Files.createDirectories(project.resolve("plans"));
        Path plan = Files.writeString(plans.resolve("test.jmx"), "");
        Files.writeString(plans.resolve("CLAUDE.md"), "local");
        Files.writeString(project.resolve("CLAUDE.md"), "root claude");
        Files.writeString(project.resolve("AGENTS.md"), "root agents");
        InstructionFilesPanel[] holder = new InstructionFilesPanel[1];

        SwingUtilities.invokeAndWait(() -> holder[0] = new InstructionFilesPanel(
                new InstructionFileLocator(), new InstructionFileStore(), () -> plan, () -> null));

        assertEquals(3, countEditors(holder[0]));
        assertEquals(List.of(
                "CLAUDE.md — Test plan directory",
                "CLAUDE.md — Git root",
                "AGENTS.md — Git root"
        ), tabTitles(holder[0]));
    }

    @Test
    void appliesWordWrappingToCurrentAndRefreshedEditors() throws Exception {
        Path project = Files.createDirectories(tempDir.resolve("project"));
        Path plan = Files.writeString(project.resolve("test.jmx"), "");
        Files.writeString(project.resolve("CLAUDE.md"), "claude");
        InstructionFilesPanel[] holder = new InstructionFilesPanel[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new InstructionFilesPanel(
                new InstructionFileLocator(), new InstructionFileStore(), () -> plan, () -> null));

        SwingUtilities.invokeAndWait(() -> holder[0].setWordWrap(true));
        assertTrue(editorsUseWordWrap(holder[0]));
        SwingUtilities.invokeAndWait(() -> holder[0].refreshFiles(true));

        assertTrue(holder[0].isWordWrapEnabled());
        assertTrue(editorsUseWordWrap(holder[0]));
    }

    @Test
    void keepsTitleAndWordWrapFromOverlappingAtNarrowWidths() throws Exception {
        Path project = Files.createDirectories(tempDir.resolve("project"));
        Path plan = Files.writeString(project.resolve("test.jmx"), "");
        InstructionFilesPanel[] holder = new InstructionFilesPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            holder[0] = new InstructionFilesPanel(
                    new InstructionFileLocator(), new InstructionFileStore(), () -> plan, () -> null);
            holder[0].setSize(360, 600);
            layoutTree(holder[0]);
        });

        JLabel title = findLabel(holder[0], "CLAUDE.md / AGENTS.md");
        JCheckBox wordWrap = findCheckBox(holder[0], "Word wrap");
        assertNotNull(findButton(holder[0], "Create…"));
        Rectangle titleBounds = SwingUtilities.convertRectangle(title.getParent(), title.getBounds(), holder[0]);
        Rectangle wordWrapBounds = SwingUtilities.convertRectangle(wordWrap.getParent(), wordWrap.getBounds(), holder[0]);

        assertFalse(titleBounds.intersects(wordWrapBounds));
        assertTrue(titleBounds.y + titleBounds.height <= wordWrapBounds.y);
    }

    @Test
    void createsMissingFilesBesideTheTestPlan() throws Exception {
        Path project = Files.createDirectories(tempDir.resolve("project"));
        Path plan = Files.writeString(project.resolve("test.jmx"), "");
        InstructionFilesPanel[] holder = new InstructionFilesPanel[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new InstructionFilesPanel(
                new InstructionFileLocator(), new InstructionFileStore(), () -> plan, () -> null));

        SwingUtilities.invokeAndWait(() -> holder[0].createFile(InstructionFileKind.AGENTS));

        assertTrue(Files.exists(project.resolve("AGENTS.md")));
        assertEquals(1, countEditors(holder[0]));
        assertEquals(List.of("AGENTS.md — Test plan directory"), tabTitles(holder[0]));
    }

    private static int countEditors(Container container) {
        int count = 0;
        for (Component component : container.getComponents()) {
            if (component instanceof InstructionFileEditorPanel) {
                count++;
            }
            if (component instanceof Container child) {
                count += countEditors(child);
            }
        }
        return count;
    }

    private static List<String> tabTitles(Container container) {
        JTabbedPane tabs = findFileTabs(container);
        List<String> titles = new java.util.ArrayList<>();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            titles.add(tabs.getTitleAt(i));
        }
        return titles;
    }

    private static JTabbedPane findFileTabs(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTabbedPane tabs
                    && tabs.getTabCount() > 0
                    && tabs.getTitleAt(0).contains(" — ")) {
                return tabs;
            }
            if (component instanceof Container child) {
                JTabbedPane result = findFileTabs(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static boolean editorsUseWordWrap(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof InstructionFileEditorPanel editor && !editor.isWordWrapEnabled()) {
                return false;
            }
            if (component instanceof Container child && !editorsUseWordWrap(child)) {
                return false;
            }
        }
        return true;
    }

    private static void layoutTree(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container child) {
                layoutTree(child);
            }
        }
    }

    private static JLabel findLabel(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel label && text.equals(label.getText())) {
                return label;
            }
            if (component instanceof Container child) {
                JLabel result = findLabel(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
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

    private static JCheckBox findCheckBox(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JCheckBox checkBox && text.equals(checkBox.getText())) {
                return checkBox;
            }
            if (component instanceof Container child) {
                JCheckBox result = findCheckBox(child, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
