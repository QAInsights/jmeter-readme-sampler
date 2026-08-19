package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.instructions.InstructionFileKind;

import javax.swing.JOptionPane;
import java.awt.Component;

final class InstructionFilesDialogs {

    private InstructionFilesDialogs() {
    }

    static boolean confirmRefresh(Component parent) {
        int choice = JOptionPane.showConfirmDialog(parent,
                "Discard unsaved changes and refresh the detected files?",
                "CLAUDE.md / AGENTS.md",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.OK_OPTION;
    }

    static void showCreateError(Component parent, InstructionFileKind kind, Throwable error) {
        JOptionPane.showMessageDialog(parent,
                error.getMessage(),
                kind.fileName(),
                JOptionPane.ERROR_MESSAGE);
    }
}
