package com.qainsights.jmeter.readme.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.prefs.Preferences;

final class InstructionFilesPreferences {

    private static final Logger logger = LoggerFactory.getLogger(InstructionFilesPreferences.class);
    private static final String VISIBLE = "instructionFilesVisible";
    private static final String WORD_WRAP = "instructionFilesWordWrap";

    private final Preferences preferences;

    InstructionFilesPreferences() {
        preferences = openPreferences();
    }

    boolean isPanelVisible() {
        return getBoolean(VISIBLE);
    }

    void setPanelVisible(boolean visible) {
        putBoolean(VISIBLE, visible);
    }

    boolean isWordWrapEnabled() {
        return getBoolean(WORD_WRAP);
    }

    void setWordWrapEnabled(boolean enabled) {
        putBoolean(WORD_WRAP, enabled);
    }

    private Preferences openPreferences() {
        try {
            return Preferences.userNodeForPackage(InstructionFilesController.class);
        } catch (SecurityException e) {
            logger.debug("Could not open the Markdown panel preferences", e);
            return null;
        }
    }

    private boolean getBoolean(String key) {
        if (preferences == null) {
            return false;
        }
        try {
            return preferences.getBoolean(key, false);
        } catch (SecurityException e) {
            logger.debug("Could not read Markdown panel preference {}", key, e);
            return false;
        }
    }

    private void putBoolean(String key, boolean value) {
        if (preferences == null) {
            return;
        }
        try {
            preferences.putBoolean(key, value);
        } catch (SecurityException e) {
            logger.debug("Could not save Markdown panel preference {}", key, e);
        }
    }
}
