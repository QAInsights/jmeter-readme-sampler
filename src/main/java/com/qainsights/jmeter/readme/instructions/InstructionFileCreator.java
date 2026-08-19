package com.qainsights.jmeter.readme.instructions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public final class InstructionFileCreator {

    private final InstructionFileLocator locator;
    private final InstructionFileStore store;
    private final Supplier<Path> testPlanFileSupplier;
    private final Supplier<Path> jmeterHomeSupplier;

    public InstructionFileCreator(InstructionFileLocator locator, InstructionFileStore store,
                                  Supplier<Path> testPlanFileSupplier, Supplier<Path> jmeterHomeSupplier) {
        this.locator = locator;
        this.store = store;
        this.testPlanFileSupplier = testPlanFileSupplier;
        this.jmeterHomeSupplier = jmeterHomeSupplier;
    }

    public boolean canCreate(InstructionFileKind kind) {
        Path directory = creationDirectory();
        return directory != null && !Files.exists(directory.resolve(kind.fileName()));
    }

    public void create(InstructionFileKind kind) throws IOException {
        Path testPlanFile = testPlanFileSupplier.get();
        Path directory = creationDirectory();
        if (directory == null) {
            throw new IllegalStateException("Save the test plan before creating " + kind.fileName());
        }
        InstructionFileSource source = testPlanFile != null
                ? InstructionFileSource.TEST_PLAN_DIRECTORY
                : InstructionFileSource.JMETER_BIN;
        store.create(directory.resolve(kind.fileName()), kind, source);
    }

    private Path creationDirectory() {
        return locator.preferredCreationDirectory(testPlanFileSupplier.get(), jmeterHomeSupplier.get());
    }
}
