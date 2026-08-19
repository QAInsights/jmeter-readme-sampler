package com.qainsights.jmeter.readme.instructions;

import java.io.IOException;
import java.nio.file.Path;

public class InstructionFileChangedException extends IOException {
    public InstructionFileChangedException(Path path) {
        super("The file changed outside JMeter: " + path);
    }
}
