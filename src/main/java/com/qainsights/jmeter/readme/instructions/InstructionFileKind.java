package com.qainsights.jmeter.readme.instructions;

public enum InstructionFileKind {
    CLAUDE("CLAUDE.md"),
    AGENTS("AGENTS.md");

    private final String fileName;

    InstructionFileKind(String fileName) {
        this.fileName = fileName;
    }

    public String fileName() {
        return fileName;
    }
}
