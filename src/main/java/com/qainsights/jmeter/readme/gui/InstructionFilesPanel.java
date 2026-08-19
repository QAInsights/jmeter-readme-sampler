package com.qainsights.jmeter.readme.gui;

import com.qainsights.jmeter.readme.instructions.DetectedInstructionFile;
import com.qainsights.jmeter.readme.instructions.InstructionFileCreator;
import com.qainsights.jmeter.readme.instructions.InstructionFileKind;
import com.qainsights.jmeter.readme.instructions.InstructionFileLocator;
import com.qainsights.jmeter.readme.instructions.InstructionFileStore;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InstructionFilesPanel extends JPanel {

    private static final String FILES_CARD = "files";
    private static final String EMPTY_CARD = "empty";

    private final InstructionFileLocator locator;
    private final InstructionFileStore store;
    private final InstructionFileCreator creator;
    private final Supplier<Path> testPlanFileSupplier;
    private final Supplier<Path> jmeterHomeSupplier;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JPanel content = new JPanel(new CardLayout());
    private final InstructionFileEditors editors = new InstructionFileEditors();
    private final InstructionFilesHeader header;
    private Runnable stateChangeListener = () -> { };
    private Runnable closeAction = () -> { };
    private Consumer<Boolean> wordWrapChangeListener = enabled -> { };

    public InstructionFilesPanel(InstructionFileLocator locator, InstructionFileStore store,
                                 Supplier<Path> testPlanFileSupplier, Supplier<Path> jmeterHomeSupplier) {
        this.locator = locator;
        this.store = store;
        this.testPlanFileSupplier = testPlanFileSupplier;
        this.jmeterHomeSupplier = jmeterHomeSupplier;
        creator = new InstructionFileCreator(locator, store, testPlanFileSupplier, jmeterHomeSupplier);
        header = createHeader();
        configureLayout();
        refreshFiles(true);
    }

    private InstructionFilesHeader createHeader() {
        InstructionFilesHeader.Callbacks callbacks = new InstructionFilesHeader.Callbacks(
                this::handleWordWrapChanged,
                () -> handleCreate(InstructionFileKind.CLAUDE),
                () -> handleCreate(InstructionFileKind.AGENTS),
                () -> canCreate(InstructionFileKind.CLAUDE),
                () -> canCreate(InstructionFileKind.AGENTS),
                this::handleRefresh,
                () -> closeAction.run());
        return new InstructionFilesHeader(callbacks);
    }

    private void configureLayout() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(650, 600));
        setMinimumSize(new Dimension(420, 300));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        content.add(tabs, FILES_CARD);
        content.add(new InstructionFilesEmptyState(
                () -> handleCreate(InstructionFileKind.CLAUDE),
                () -> handleCreate(InstructionFileKind.AGENTS)), EMPTY_CARD);
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public boolean hasUnsavedChanges() {
        return editors.hasUnsavedChanges();
    }

    public void setStateChangeListener(Runnable listener) {
        stateChangeListener = listener != null ? listener : () -> { };
        editors.setStateChangeListener(stateChangeListener);
    }

    public void setCloseAction(Runnable action) {
        closeAction = action != null ? action : () -> { };
    }

    public void setWordWrap(boolean enabled) {
        header.setWordWrap(enabled);
        applyWordWrap(enabled);
    }

    public boolean isWordWrapEnabled() {
        return header.isWordWrapEnabled();
    }

    public void setWordWrapChangeListener(Consumer<Boolean> listener) {
        wordWrapChangeListener = listener != null ? listener : enabled -> { };
    }

    public void saveAll() {
        editors.saveAll();
    }

    public void discardAll() {
        editors.discardAll();
    }

    public void refreshFiles(boolean discardUnsavedChanges) {
        if (hasUnsavedChanges() && !discardUnsavedChanges) {
            return;
        }
        editors.clear();
        tabs.removeAll();

        List<DetectedInstructionFile> detected = locator.locate(testPlanFileSupplier.get(), jmeterHomeSupplier.get());
        for (DetectedInstructionFile file : detected) {
            try {
                InstructionFileEditorPanel editor = new InstructionFileEditorPanel(store.load(file), store);
                editor.setStateChangeListener(stateChangeListener);
                editor.setWordWrap(header.isWordWrapEnabled());
                editors.add(editor);
                addFileTab(file, editor);
            } catch (IOException e) {
                JTextArea error = new JTextArea("Could not load " + file.path() + ":\n" + e.getMessage());
                error.setEditable(false);
                addFileTab(file, new JScrollPane(error));
            }
        }
        CardLayout layout = (CardLayout) content.getLayout();
        layout.show(content, tabs.getTabCount() > 0 ? FILES_CARD : EMPTY_CARD);
        revalidate();
        repaint();
        stateChangeListener.run();
    }

    public void createFile(InstructionFileKind kind) {
        try {
            creator.create(kind);
            refreshFiles(true);
            selectFile(kind);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void dispose() {
        editors.clear();
    }

    private void selectFile(InstructionFileKind kind) {
        String prefix = kind.fileName() + " — ";
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (tabs.getTitleAt(i).startsWith(prefix)) {
                tabs.setSelectedIndex(i);
                return;
            }
        }
    }

    private void addFileTab(DetectedInstructionFile file, Component component) {
        String title = file.kind().fileName() + " — " + file.source().label();
        tabs.addTab(title, component);
        tabs.setTabComponentAt(tabs.getTabCount() - 1, new InstructionFileTabLabel(file));
    }

    private void applyWordWrap(boolean enabled) {
        editors.setWordWrap(enabled);
    }

    private void handleWordWrapChanged(boolean enabled) {
        applyWordWrap(enabled);
        wordWrapChangeListener.accept(enabled);
    }

    private boolean canCreate(InstructionFileKind kind) {
        return creator.canCreate(kind);
    }

    private void handleRefresh() {
        if (!hasUnsavedChanges() || InstructionFilesDialogs.confirmRefresh(this)) {
            refreshFiles(true);
        }
    }

    private void handleCreate(InstructionFileKind kind) {
        try {
            createFile(kind);
        } catch (RuntimeException e) {
            Throwable error = e instanceof UncheckedIOException ? e.getCause() : e;
            InstructionFilesDialogs.showCreateError(this, kind, error);
        }
    }
}
