package com.qainsights.jmeter.readme.gui;

import org.apache.jmeter.gui.plugin.MenuCreator;
import org.junit.jupiter.api.Test;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstructionFilesMenuCreatorTest {

    @Test
    void contributesOneCheckBoxToTheOptionsMenu() {
        InstructionFilesMenuCreator creator = new InstructionFilesMenuCreator();

        JMenuItem[] items = creator.getMenuItemsAtLocation(MenuCreator.MENU_LOCATION.OPTIONS);

        assertEquals(1, items.length);
        JCheckBoxMenuItem item = assertInstanceOf(JCheckBoxMenuItem.class, items[0]);
        assertEquals("Show CLAUDE.md / AGENTS.md", item.getText());
        assertEquals(0, creator.getMenuItemsAtLocation(MenuCreator.MENU_LOCATION.FILE).length);
    }

    @Test
    void registersTheMenuCreatorAsAService() throws IOException {
        String resource = "META-INF/services/org.apache.jmeter.gui.plugin.MenuCreator";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertTrue(input != null);
            assertEquals(InstructionFilesMenuCreator.class.getName(),
                    new String(input.readAllBytes(), StandardCharsets.UTF_8).trim());
        }
    }
}
