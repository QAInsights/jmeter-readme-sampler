package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.instructions.InstructionFileChangedException;
import com.qainsights.jmeter.readme.instructions.InstructionFileStore;
import com.qainsights.jmeter.readme.instructions.LoadedInstructionFile;
import com.qainsights.jmeter.readme.readme.ReadMeMarkdownRenderer;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.UncheckedIOException;

public class InstructionFileEditorPanel extends JPanel {

    private final InstructionFileStore store;
    private final JTextArea editor = new JTextArea();
    private final JEditorPane preview = new JEditorPane("text/html", "");
    private final InstructionFileEditorHeader header;
    private final Timer renderTimer;
    private LoadedInstructionFile loaded;
    private boolean updating;
    private boolean dirty;
    private Runnable stateChangeListener = () -> { };

    public InstructionFileEditorPanel(LoadedInstructionFile loaded, InstructionFileStore store) {
        this.loaded = loaded;
        this.store = store;
        InstructionFileEditorHeader.Callbacks callbacks = new InstructionFileEditorHeader.Callbacks(
                this::handleSave, this::handleReload, this::revertChanges);
        header = new InstructionFileEditorHeader(loaded, callbacks);
        renderTimer = new Timer(300, e -> render());
        renderTimer.setRepeats(false);
        configureEditor();
        configureLayout();
        applyLoadedFile(loaded);
    }

    private void configureEditor() {
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        editor.setLineWrap(false);
        editor.getDocument().addDocumentListener(new SimpleDocumentListener(this::documentChanged));
        preview.setEditable(false);
        preview.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        preview.addHyperlinkListener(event -> MarkdownHyperlinkHandler.handle(this, event));
    }

    private void configureLayout() {
        setLayout(new BorderLayout(0, 10));
        add(header, BorderLayout.NORTH);
        add(createEditorTabs(), BorderLayout.CENTER);
    }

    private JTabbedPane createEditorTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Write", new JScrollPane(editor));
        tabs.addTab("Preview", new JScrollPane(preview));
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) {
                render();
            }
        });
        return tabs;
    }

    public boolean hasUnsavedChanges() {
        return dirty;
    }

    public String getEditorText() {
        return editor.getText();
    }

    public void setEditorText(String content) {
        editor.setText(content);
    }

    public void setWordWrap(boolean enabled) {
        editor.setLineWrap(enabled);
        editor.setWrapStyleWord(enabled);
    }

    public boolean isWordWrapEnabled() {
        return editor.getLineWrap();
    }

    public LoadedInstructionFile getLoadedFile() {
        return loaded;
    }

    public void setStateChangeListener(Runnable listener) {
        stateChangeListener = listener != null ? listener : () -> { };
    }

    public void saveChanges() {
        try {
            applyLoadedFile(store.save(loaded, editor.getText()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void reloadFromDisk() {
        try {
            applyLoadedFile(store.load(loaded.file()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void revertChanges() {
        applyLoadedFile(loaded);
    }

    public void dispose() {
        renderTimer.stop();
    }

    private void applyLoadedFile(LoadedInstructionFile file) {
        loaded = file;
        updating = true;
        editor.setText(file.content());
        editor.setCaretPosition(0);
        editor.setEditable(file.writable());
        header.updateFile(file);
        updating = false;
        dirty = false;
        updateActions();
        render();
        stateChangeListener.run();
    }

    private void documentChanged() {
        if (updating) {
            return;
        }
        dirty = !editor.getText().equals(loaded.content());
        renderTimer.restart();
        updateActions();
        stateChangeListener.run();
    }

    private void updateActions() {
        header.updateActions(loaded.writable(), dirty);
    }

    private void render() {
        preview.setText(new ReadMeMarkdownRenderer().render(editor.getText()));
        preview.setCaretPosition(0);
    }

    private void handleSave() {
        try {
            saveChanges();
        } catch (UncheckedIOException e) {
            if (e.getCause() instanceof InstructionFileChangedException) {
                JOptionPane.showMessageDialog(this,
                        "The file changed outside JMeter. Reload it before saving your changes.",
                        loaded.file().kind().fileName(), JOptionPane.WARNING_MESSAGE);
            } else {
                showError("Could not save the file", e.getCause());
            }
        }
    }

    private void handleReload() {
        if (dirty) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Discard unsaved changes and reload the file?",
                    loaded.file().kind().fileName(), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
        }
        try {
            reloadFromDisk();
        } catch (UncheckedIOException e) {
            showError("Could not reload the file", e.getCause());
        }
    }

    private void showError(String message, Throwable error) {
        JOptionPane.showMessageDialog(this,
                message + ": " + error.getMessage(),
                loaded.file().kind().fileName(), JOptionPane.ERROR_MESSAGE);
    }
}
