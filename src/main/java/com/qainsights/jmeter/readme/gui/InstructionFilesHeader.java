package com.qainsights.jmeter.readme.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

final class InstructionFilesHeader extends JPanel {

    record Callbacks(
            Consumer<Boolean> wordWrapChanged,
            Runnable createClaude,
            Runnable createAgents,
            BooleanSupplier canCreateClaude,
            BooleanSupplier canCreateAgents,
            Runnable refresh,
            Runnable close) {
    }

    private final JCheckBox wordWrap = new JCheckBox("Word wrap");
    private final JMenuItem createClaude = new JMenuItem("CLAUDE.md");
    private final JMenuItem createAgents = new JMenuItem("AGENTS.md");
    private final Callbacks callbacks;

    InstructionFilesHeader(Callbacks callbacks) {
        this.callbacks = callbacks;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        JPanel titleRow = createTitleRow();
        JPanel toolbar = createToolbar();
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titleRow);
        add(Box.createVerticalStrut(8));
        add(toolbar);
        add(Box.createVerticalStrut(10));
        add(new JSeparator());
    }

    void setWordWrap(boolean enabled) {
        wordWrap.setSelected(enabled);
    }

    boolean isWordWrapEnabled() {
        return wordWrap.isSelected();
    }

    private JPanel createTitleRow() {
        JLabel title = new JLabel("CLAUDE.md / AGENTS.md");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JButton close = new JButton("×");
        close.setToolTipText("Close panel");
        close.getAccessibleContext().setAccessibleName("Close panel");
        close.setMargin(new Insets(2, 8, 2, 8));
        close.addActionListener(e -> callbacks.close().run());
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(title, BorderLayout.WEST);
        row.add(close, BorderLayout.EAST);
        return row;
    }

    private JPanel createToolbar() {
        wordWrap.addActionListener(e -> callbacks.wordWrapChanged().accept(wordWrap.isSelected()));
        JButton create = createMenuButton();
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> callbacks.refresh().run());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.add(create);
        actions.add(refresh);
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.add(wordWrap, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);
        return toolbar;
    }

    private JButton createMenuButton() {
        createClaude.addActionListener(e -> callbacks.createClaude().run());
        createAgents.addActionListener(e -> callbacks.createAgents().run());
        JPopupMenu menu = new JPopupMenu();
        menu.add(createClaude);
        menu.add(createAgents);
        JButton create = new JButton("Create…");
        create.addActionListener(e -> {
            updateCreateActions();
            menu.show(create, 0, create.getHeight());
        });
        return create;
    }

    private void updateCreateActions() {
        createClaude.setEnabled(callbacks.canCreateClaude().getAsBoolean());
        createAgents.setEnabled(callbacks.canCreateAgents().getAsBoolean());
    }
}
