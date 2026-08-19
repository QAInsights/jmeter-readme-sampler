package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.instructions.DetectedInstructionFile;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

final class InstructionFileTabLabel extends JPanel {

    InstructionFileTabLabel(DetectedInstructionFile file) {
        JLabel name = new JLabel(file.kind().fileName());
        name.setFont(name.getFont().deriveFont(Font.BOLD, 12f));
        JLabel source = new JLabel(file.source().label());
        source.setFont(source.getFont().deriveFont(10f));
        Color secondaryText = UIManager.getColor("Label.disabledForeground");
        if (secondaryText != null) {
            source.setForeground(secondaryText);
        }
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        source.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(name);
        add(source);
    }
}
