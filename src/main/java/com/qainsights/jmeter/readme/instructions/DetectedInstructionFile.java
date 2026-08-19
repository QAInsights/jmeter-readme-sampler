package com.qainsights.jmeter.readme.instructions;

import java.nio.file.Path;

public record DetectedInstructionFile(Path path, InstructionFileKind kind, InstructionFileSource source) {
    public DetectedInstructionFile {
        path = path.toAbsolutePath().normalize();
    }
}
