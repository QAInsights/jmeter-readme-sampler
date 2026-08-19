package com.qainsights.jmeter.readme.instructions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InstructionFileLocator {

    public List<DetectedInstructionFile> locate(Path testPlanFile, Path jmeterHome) {
        Map<Path, InstructionFileSource> directories = new LinkedHashMap<>();
        Path testPlanDirectory = testPlanDirectory(testPlanFile);
        addDirectory(directories, testPlanDirectory, InstructionFileSource.TEST_PLAN_DIRECTORY);
        addDirectory(directories, findGitRoot(testPlanDirectory), InstructionFileSource.GIT_ROOT);
        addDirectory(directories, jmeterBin(jmeterHome), InstructionFileSource.JMETER_BIN);

        List<DetectedInstructionFile> files = new ArrayList<>();
        Set<Path> detectedPaths = new LinkedHashSet<>();
        for (Map.Entry<Path, InstructionFileSource> entry : directories.entrySet()) {
            for (InstructionFileKind kind : InstructionFileKind.values()) {
                Path candidate = entry.getKey().resolve(kind.fileName());
                if (Files.isRegularFile(candidate)) {
                    Path resolved = canonical(candidate);
                    if (detectedPaths.add(resolved)) {
                        files.add(new DetectedInstructionFile(resolved, kind, entry.getValue()));
                    }
                }
            }
        }
        return List.copyOf(files);
    }

    public Path preferredCreationDirectory(Path testPlanFile, Path jmeterHome) {
        Path testPlanDirectory = testPlanDirectory(testPlanFile);
        return testPlanDirectory != null ? testPlanDirectory : jmeterBin(jmeterHome);
    }

    private void addDirectory(Map<Path, InstructionFileSource> directories, Path directory,
                              InstructionFileSource source) {
        if (directory == null || !Files.isDirectory(directory)) {
            return;
        }
        directories.putIfAbsent(canonical(directory), source);
    }

    private Path testPlanDirectory(Path testPlanFile) {
        if (testPlanFile == null) {
            return null;
        }
        Path absolute = testPlanFile.toAbsolutePath().normalize();
        return Files.isDirectory(absolute) ? absolute : absolute.getParent();
    }

    private Path findGitRoot(Path start) {
        Path current = start;
        while (current != null) {
            if (Files.exists(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    private Path jmeterBin(Path jmeterHome) {
        return jmeterHome == null ? null : jmeterHome.toAbsolutePath().normalize().resolve("bin");
    }

    private Path canonical(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException ignored) {
            return path.toAbsolutePath().normalize();
        }
    }
}
