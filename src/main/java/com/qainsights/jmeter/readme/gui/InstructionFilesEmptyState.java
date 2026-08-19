package com.qainsights.jmeter.readme.gui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

final class InstructionFilesEmptyState extends JPanel {

    InstructionFilesEmptyState(Runnable createClaude, Runnable createAgents) {
        setLayout(new GridBagLayout());
        add(createBody(createClaude, createAgents));
    }

    private JPanel createBody(Runnable createClaude, Runnable createAgents) {
        JLabel heading = new JLabel("No Markdown files found");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 15f));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel message = createMessage();
        JPanel actions = createActions(createClaude, createAgents);
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(heading);
        body.add(Box.createVerticalStrut(6));
        body.add(message);
        body.add(Box.createVerticalStrut(14));
        body.add(actions);
        return body;
    }

    private JLabel createMessage() {
        JLabel message = new JLabel("Create a project file or refresh after adding one on disk.");
        message.setAlignmentX(Component.CENTER_ALIGNMENT);
        Color secondaryText = UIManager.getColor("Label.disabledForeground");
        if (secondaryText != null) {
            message.setForeground(secondaryText);
        }
        return message;
    }

    private JPanel createActions(Runnable createClaude, Runnable createAgents) {
        JButton claude = new JButton("Create CLAUDE.md");
        claude.addActionListener(e -> createClaude.run());
        JButton agents = new JButton("Create AGENTS.md");
        agents.addActionListener(e -> createAgents.run());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);
        actions.add(claude);
        actions.add(agents);
        return actions;
    }
}
