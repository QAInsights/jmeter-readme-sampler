package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.instructions.LoadedInstructionFile;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

final class InstructionFileEditorHeader extends JPanel {

    record Callbacks(Runnable save, Runnable reload, Runnable revert) {
    }

    private final JLabel fileName = new JLabel();
    private final JLabel source = new JLabel();
    private final JLabel path = new JLabel();
    private final JButton save = new JButton("Save");
    private final JMenuItem reload = new JMenuItem("Reload from disk");
    private final JMenuItem revert = new JMenuItem("Revert changes");

    InstructionFileEditorHeader(LoadedInstructionFile file, Callbacks callbacks) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 12));
        configureLabels();
        add(createTitleRow(callbacks));
        add(Box.createVerticalStrut(2));
        add(source);
        add(Box.createVerticalStrut(2));
        add(path);
        add(Box.createVerticalStrut(8));
        add(new JSeparator());
        updateFile(file);
    }

    void updateFile(LoadedInstructionFile file) {
        fileName.setText(file.file().kind().fileName());
        source.setText(file.file().source().label() + (file.writable() ? "" : " · Read only"));
        path.setText(file.file().path().toString());
        path.setToolTipText(file.file().path().toString());
    }

    void updateActions(boolean writable, boolean dirty) {
        save.setEnabled(writable && dirty);
        revert.setEnabled(dirty);
        reload.setEnabled(true);
    }

    private void configureLabels() {
        fileName.setFont(fileName.getFont().deriveFont(Font.BOLD, 15f));
        source.setFont(source.getFont().deriveFont(11f));
        path.setFont(path.getFont().deriveFont(11f));
        Color secondaryText = UIManager.getColor("Label.disabledForeground");
        if (secondaryText != null) {
            source.setForeground(secondaryText);
            path.setForeground(secondaryText);
        }
        source.setAlignmentX(Component.LEFT_ALIGNMENT);
        path.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JPanel createTitleRow(Callbacks callbacks) {
        save.addActionListener(e -> callbacks.save().run());
        reload.addActionListener(e -> callbacks.reload().run());
        revert.addActionListener(e -> callbacks.revert().run());
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(fileName, BorderLayout.WEST);
        row.add(createPrimaryActions(), BorderLayout.EAST);
        return row;
    }

    private JPanel createPrimaryActions() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(reload);
        menu.add(revert);
        JButton more = new JButton("⋮");
        more.setToolTipText("More actions");
        more.getAccessibleContext().setAccessibleName("More file actions");
        more.setMargin(new Insets(2, 8, 2, 8));
        more.addActionListener(e -> menu.show(more, 0, more.getHeight()));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.add(save);
        actions.add(more);
        return actions;
    }
}
