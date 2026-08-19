package com.qainsights.jmeter.readme.gui;

import org.apache.jmeter.gui.plugin.MenuCreator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

public class InstructionFilesMenuCreator implements MenuCreator {

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if (location != MENU_LOCATION.OPTIONS) {
            return new JMenuItem[0];
        }
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Show CLAUDE.md / AGENTS.md");
        InstructionFilesController manager = InstructionFilesController.getInstance();
        manager.registerMenuItem(item);
        item.addActionListener(e -> manager.setVisible(item.isSelected()));
        return new JMenuItem[]{item};
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
    }
}
