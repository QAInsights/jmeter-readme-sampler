package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.protocol.JMeterProtocolHandler;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

final class MarkdownHyperlinkHandler {

    private MarkdownHyperlinkHandler() {
    }

    static void handle(Component parent, HyperlinkEvent event) {
        if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }
        String href = event.getURL() != null ? event.getURL().toString() : event.getDescription();
        if (href == null) {
            return;
        }
        if (href.startsWith(JMeterProtocolHandler.PROTOCOL)) {
            navigate(parent, href);
        } else {
            browse(event, href);
        }
    }

    static String nodeName(String href) {
        String encodedName = href.substring(JMeterProtocolHandler.PROTOCOL.length());
        return URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
    }

    private static void navigate(Component parent, String href) {
        String name = nodeName(href);
        if (!JMeterProtocolHandler.navigate(name)) {
            JOptionPane.showMessageDialog(
                    SwingUtilities.getWindowAncestor(parent),
                    "Node not found: " + name,
                    "Markdown link",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void browse(HyperlinkEvent event, String href) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                return;
            }
            URI uri = event.getURL() != null ? event.getURL().toURI() : URI.create(href);
            desktop.browse(uri);
        } catch (Exception ignored) {
        }
    }
}
