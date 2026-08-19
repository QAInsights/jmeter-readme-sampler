package com.qainsights.jmeter.readme.gui;

import java.util.ArrayList;
import java.util.List;

final class InstructionFileEditors {

    private final List<InstructionFileEditorPanel> editors = new ArrayList<>();

    void add(InstructionFileEditorPanel editor) {
        editors.add(editor);
    }

    boolean hasUnsavedChanges() {
        return editors.stream().anyMatch(InstructionFileEditorPanel::hasUnsavedChanges);
    }

    void setStateChangeListener(Runnable listener) {
        editors.forEach(editor -> editor.setStateChangeListener(listener));
    }

    void setWordWrap(boolean enabled) {
        editors.forEach(editor -> editor.setWordWrap(enabled));
    }

    void saveAll() {
        editors.stream()
                .filter(InstructionFileEditorPanel::hasUnsavedChanges)
                .forEach(InstructionFileEditorPanel::saveChanges);
    }

    void discardAll() {
        editors.stream()
                .filter(InstructionFileEditorPanel::hasUnsavedChanges)
                .forEach(InstructionFileEditorPanel::revertChanges);
    }

    void clear() {
        editors.forEach(InstructionFileEditorPanel::dispose);
        editors.clear();
    }
}
