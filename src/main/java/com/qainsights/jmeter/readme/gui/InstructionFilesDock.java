package com.qainsights.jmeter.readme.gui;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

public class InstructionFilesDock {

    private Container contentPane;
    private Component mainComponent;
    private Object originalConstraint;
    private JSplitPane splitPane;

    public void show(Container contentPane, JComponent filesPanel) {
        if (isShowing()) {
            return;
        }
        Component center = findCenterComponent(contentPane);
        if (center == null) {
            throw new IllegalStateException("Could not find JMeter's main content component");
        }
        this.contentPane = contentPane;
        mainComponent = center;
        originalConstraint = findLayoutConstraint(contentPane, center);
        contentPane.remove(center);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, center, filesPanel);
        splitPane.setResizeWeight(0.68);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        addAtOriginalConstraint(splitPane);
        contentPane.revalidate();
        contentPane.repaint();
        JSplitPane displayedSplitPane = splitPane;
        SwingUtilities.invokeLater(() -> {
            if (displayedSplitPane.getParent() != null) {
                displayedSplitPane.setDividerLocation(0.68);
            }
        });
    }

    public void hide() {
        if (!isShowing()) {
            return;
        }
        contentPane.remove(splitPane);
        addAtOriginalConstraint(mainComponent);
        contentPane.revalidate();
        contentPane.repaint();
        contentPane = null;
        mainComponent = null;
        originalConstraint = null;
        splitPane = null;
    }

    public boolean isShowing() {
        return contentPane != null && splitPane != null && splitPane.getParent() == contentPane;
    }

    private void addAtOriginalConstraint(Component component) {
        if (originalConstraint == null) {
            contentPane.add(component);
        } else {
            contentPane.add(component, originalConstraint);
        }
    }

    private Object findLayoutConstraint(Container container, Component component) {
        if (container.getLayout() instanceof BorderLayout borderLayout) {
            return borderLayout.getConstraints(component);
        }
        return null;
    }

    private Component findCenterComponent(Container contentPane) {
        LayoutManager layout = contentPane.getLayout();
        if (layout instanceof BorderLayout borderLayout) {
            for (Component component : contentPane.getComponents()) {
                if (BorderLayout.CENTER.equals(borderLayout.getConstraints(component))) {
                    return component;
                }
            }
        }
        return contentPane.getComponentCount() == 1 ? contentPane.getComponent(0) : null;
    }
}
