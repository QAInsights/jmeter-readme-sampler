package com.qainsights.jmeter.readme.instructions;

public record LoadedInstructionFile(
        DetectedInstructionFile file,
        String content,
        String lineSeparator,
        boolean byteOrderMark,
        String revision,
        boolean writable) {
}
