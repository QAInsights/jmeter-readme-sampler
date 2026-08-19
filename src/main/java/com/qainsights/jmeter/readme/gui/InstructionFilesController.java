package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.instructions.InstructionFileLocator;
import com.qainsights.jmeter.readme.instructions.InstructionFileStore;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.GraphicsEnvironment;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public final class InstructionFilesController {

    private static final Logger logger = LoggerFactory.getLogger(InstructionFilesController.class);
    private static final InstructionFilesController INSTANCE = new InstructionFilesController();

    private final InstructionFilesDock dock = new InstructionFilesDock();
    private final InstructionFilesPreferences preferences = new InstructionFilesPreferences();
    private JCheckBoxMenuItem menuItem;
    private InstructionFilesPanel panel;
    private Timer restoreTimer;

    private InstructionFilesController() {
    }

    public static InstructionFilesController getInstance() {
        return INSTANCE;
    }

    public void registerMenuItem(JCheckBoxMenuItem item) {
        menuItem = item;
        boolean visible = preferences.isPanelVisible();
        item.setSelected(visible);
        if (visible && !GraphicsEnvironment.isHeadless()) {
            restoreWhenReady();
        }
    }

    public void setVisible(boolean visible) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setVisible(visible));
            return;
        }
        if (visible) {
            showPanel();
        } else {
            hidePanel();
        }
    }

    public boolean isVisible() {
        return dock.isShowing();
    }

    private void showPanel() {
        if (dock.isShowing()) {
            updateMenuSelection(true);
            return;
        }
        GuiPackage gui = GuiPackage.getInstance();
        if (gui == null || gui.getMainFrame() == null) {
            updateMenuSelection(false);
            return;
        }
        try {
            panel = new InstructionFilesPanel(new InstructionFileLocator(), new InstructionFileStore(),
                    this::currentTestPlanFile, this::jmeterHome);
            panel.setWordWrap(preferences.isWordWrapEnabled());
            panel.setWordWrapChangeListener(preferences::setWordWrapEnabled);
            panel.setCloseAction(() -> setVisible(false));
            dock.show(gui.getMainFrame().getContentPane(), panel);
            updateMenuSelection(true);
            preferences.setPanelVisible(true);
        } catch (RuntimeException e) {
            if (panel != null) {
                panel.dispose();
                panel = null;
            }
            updateMenuSelection(false);
            showError("Could not show CLAUDE.md / AGENTS.md", e);
        }
    }

    private void hidePanel() {
        stopRestoreTimer();
        if (!dock.isShowing()) {
            updateMenuSelection(false);
            preferences.setPanelVisible(false);
            return;
        }
        if (panel != null && panel.hasUnsavedChanges() && !resolveUnsavedChanges()) {
            updateMenuSelection(true);
            return;
        }
        dock.hide();
        if (panel != null) {
            panel.dispose();
            panel = null;
        }
        updateMenuSelection(false);
        preferences.setPanelVisible(false);
    }

    private boolean resolveUnsavedChanges() {
        Object[] options = {"Save and Hide", "Discard and Hide", "Cancel"};
        int choice = JOptionPane.showOptionDialog(panel,
                "CLAUDE.md or AGENTS.md has unsaved changes.",
                "Unsaved Markdown changes", JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            try {
                panel.saveAll();
                return true;
            } catch (UncheckedIOException e) {
                showError("Could not save all changed files", e.getCause());
                return false;
            }
        }
        if (choice == 1) {
            panel.discardAll();
            return true;
        }
        return false;
    }

    private Path currentTestPlanFile() {
        GuiPackage gui = GuiPackage.getInstance();
        if (gui == null) {
            return null;
        }
        String file = gui.getTestPlanFile();
        return file == null || file.isBlank() ? null : Path.of(file);
    }

    private Path jmeterHome() {
        String home = JMeterUtils.getJMeterHome();
        return home == null || home.isBlank() ? null : Path.of(home);
    }

    private void updateMenuSelection(boolean selected) {
        if (menuItem != null) {
            menuItem.setSelected(selected);
        }
    }

    private void restoreWhenReady() {
        stopRestoreTimer();
        int[] attempts = {0};
        restoreTimer = new Timer(250, null);
        restoreTimer.addActionListener(e -> {
            GuiPackage gui = GuiPackage.getInstance();
            if (gui != null && gui.getMainFrame() != null) {
                stopRestoreTimer();
                setVisible(true);
            } else if (++attempts[0] >= 20) {
                stopRestoreTimer();
                updateMenuSelection(false);
            }
        });
        restoreTimer.start();
    }

    private void stopRestoreTimer() {
        if (restoreTimer != null) {
            restoreTimer.stop();
            restoreTimer = null;
        }
    }

    private void showError(String message, Throwable error) {
        logger.error(message, error);
        JOptionPane.showMessageDialog(panel,
                message + ": " + error.getMessage(),
                "CLAUDE.md / AGENTS.md", JOptionPane.ERROR_MESSAGE);
    }
}
