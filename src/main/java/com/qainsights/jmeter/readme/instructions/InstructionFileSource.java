package com.qainsights.jmeter.readme.instructions;

public enum InstructionFileSource {
    TEST_PLAN_DIRECTORY("Test plan directory"),
    GIT_ROOT("Git root"),
    JMETER_BIN("JMeter bin");

    private final String label;

    InstructionFileSource(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
