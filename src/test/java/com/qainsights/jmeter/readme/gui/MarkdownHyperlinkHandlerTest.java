package com.qainsights.jmeter.readme.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkdownHyperlinkHandlerTest {

    @Test
    void decodesJMeterNodeNamesForNavigationAndMessages() {
        assertEquals("Login Request",
                MarkdownHyperlinkHandler.nodeName("jmeter://Login%20Request"));
        assertEquals("Résumé",
                MarkdownHyperlinkHandler.nodeName("jmeter://R%C3%A9sum%C3%A9"));
    }
}
