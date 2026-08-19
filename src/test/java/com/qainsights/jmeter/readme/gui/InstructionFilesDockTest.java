package com.qainsights.jmeter.readme.gui;

import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructionFilesDockTest {

    @Test
    void restoresTheOriginalBorderLayoutConstraint() throws Exception {
        BorderLayout layout = new BorderLayout();
        JPanel content = new JPanel(layout);
        JLabel main = new JLabel("main");
        JPanel files = new JPanel();
        content.add(main, BorderLayout.PAGE_START);
        InstructionFilesDock dock = new InstructionFilesDock();

        SwingUtilities.invokeAndWait(() -> dock.show(content, files));
        JSplitPane split = (JSplitPane) content.getComponent(0);
        assertSame(split, layout.getLayoutComponent(BorderLayout.PAGE_START));

        SwingUtilities.invokeAndWait(dock::hide);
        assertSame(main, layout.getLayoutComponent(BorderLayout.PAGE_START));
    }

    @Test
    void docksAndRestoresTheExistingCenterComponent() throws Exception {
        JPanel content = new JPanel(new BorderLayout());
        JLabel main = new JLabel("main");
        JPanel files = new JPanel();
        content.add(main, BorderLayout.CENTER);
        InstructionFilesDock dock = new InstructionFilesDock();

        SwingUtilities.invokeAndWait(() -> dock.show(content, files));

        assertTrue(dock.isShowing());
        assertEquals(1, content.getComponentCount());
        JSplitPane split = (JSplitPane) content.getComponent(0);
        assertSame(main, split.getLeftComponent());
        assertSame(files, split.getRightComponent());

        SwingUtilities.invokeAndWait(dock::hide);

        assertFalse(dock.isShowing());
        assertEquals(1, content.getComponentCount());
        assertSame(main, content.getComponent(0));
    }
}
