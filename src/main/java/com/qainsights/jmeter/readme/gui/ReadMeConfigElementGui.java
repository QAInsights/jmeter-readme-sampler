package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.readme.ReadMeConfigElement;
import com.qainsights.jmeter.readme.readme.ReadMeMarkdownRenderer;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

public class ReadMeConfigElementGui extends AbstractConfigGui {

    private static final Logger logger = LoggerFactory.getLogger(ReadMeConfigElementGui.class);
    private Timer       debounceTimer;
    private JScrollPane  previewScroll;
    JTextArea markdownInput = new JTextArea();
    JEditorPane previewPane = new JEditorPane("text/html", "") {
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
    };



    public ReadMeConfigElementGui() {
       logger.debug("Initializing ReadMeConfigElementGui");
       init();
    }

    private void init() {
        logger.debug("Setting up GUI layout");
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        // Title
        add(makeTitlePanel(), BorderLayout.NORTH);
        add(createEditorTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane createEditorTabs() {
        // Tabbed pane: Write | Preview (GitHub-style)
        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.addTab("Write", createWriteScrollPane());
        tabs.addTab("Preview", createPreviewScrollPane());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                doRender();
            }
        });
        return tabs;
    }

    private JScrollPane createWriteScrollPane() {
        // Write tab: markdown editor
        markdownInput.setFont(new Font("Monospaced", Font.PLAIN, 13));
        markdownInput.setLineWrap(false);
        debounceTimer = new Timer(300, e -> doRender());
        debounceTimer.setRepeats(false);
        markdownInput.getDocument().addDocumentListener(
                new SimpleDocumentListener(debounceTimer::restart));
        JScrollPane scroll = new JScrollPane(markdownInput);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return scroll;
    }

    private JScrollPane createPreviewScrollPane() {
        // Preview tab: HTML preview
        configurePreviewPane();
        previewScroll = new JScrollPane(previewPane);
        previewScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        previewScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        return previewScroll;
    }

    private void configurePreviewPane() {
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { margin: 0; padding: 0; }");
        styleSheet.addRule("p { margin-top: 0; margin-bottom: 0; }");
        styleSheet.addRule("h1,h2,h3,h4,h5,h6 { margin-top: 0; margin-bottom: 0; }");
        styleSheet.addRule("ul,ol { margin-top: 0; margin-bottom: 0; }");
        previewPane.setEditorKit(kit);
        previewPane.setEditable(false);
        previewPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        previewPane.setBackground(Color.WHITE);
        previewPane.addHyperlinkListener(event -> MarkdownHyperlinkHandler.handle(this, event));
    }

    @Override
    public Dimension getPreferredSize() {
        // Stretch to the full width of JMeter's right pane, but bound the
        // height so the inner JScrollPanes (Write/Preview) actually receive
        // overflow instead of the panel growing inside JMeter's outer scroll.
        Dimension d = super.getPreferredSize();
        Container parent = getParent();
        if (parent != null && parent.getWidth() > 0) {
            d.width = parent.getWidth();
        }
        d.height = 600;
        return d;
    }

    private void doRender() {
        JViewport viewport = previewScroll.getViewport();
        Point     savedPos = viewport.getViewPosition();
        String    html     = new ReadMeMarkdownRenderer().render(markdownInput.getText());

        previewPane.setText(html);

        SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
            try {
                viewport.setViewPosition(savedPos);
            } catch (Exception ignored) {}
        }));
        logger.debug("Preview updated successfully");
    }

    @Override
    public String getLabelResource() {
        return "README Config Element";
    }

    @Override
    public String getStaticLabel() {
        return "README Config Element";
    }

    @Override
    public void modifyTestElement(TestElement element) {
        logger.debug("Saving markdown content to config element");

        super.modifyTestElement(element);
        if (element instanceof ReadMeConfigElement) {
            ((ReadMeConfigElement) element).setMarkdownContent(markdownInput.getText());
        }
    }

    @Override
    public void configure(TestElement element) {
        logger.debug("Loading markdown content from config element");
        super.configure(element);
        if (element instanceof ReadMeConfigElement) {
            markdownInput.setText(((ReadMeConfigElement) element).getMarkdownContent());
        }
    }

    @Override
    public TestElement createTestElement() {
        logger.debug("Creating test element");
        ReadMeConfigElement readMeConfigElement = new ReadMeConfigElement();
        modifyTestElement(readMeConfigElement);
        logger.debug("Test element created and modified");
        return readMeConfigElement;
    }
}
