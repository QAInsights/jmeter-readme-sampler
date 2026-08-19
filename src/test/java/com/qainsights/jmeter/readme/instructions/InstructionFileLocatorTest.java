package com.qainsights.jmeter.readme.instructions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstructionFileLocatorTest {

    @TempDir
    Path tempDir;

    private final InstructionFileLocator locator = new InstructionFileLocator();

    @Test
    void detectsClaudeAndAgentsBesideTestPlan() throws IOException {
        Path project = Files.createDirectories(tempDir.resolve("project"));
        Path plan = Files.writeString(project.resolve("checkout.jmx"), "");
        Files.writeString(project.resolve("CLAUDE.md"), "claude");
        Files.writeString(project.resolve("AGENTS.md"), "agents");

        List<DetectedInstructionFile> files = locator.locate(plan, null);

        assertEquals(List.of(
                new DetectedInstructionFile(project.resolve("CLAUDE.md"), InstructionFileKind.CLAUDE,
                        InstructionFileSource.TEST_PLAN_DIRECTORY),
                new DetectedInstructionFile(project.resolve("AGENTS.md"), InstructionFileKind.AGENTS,
                        InstructionFileSource.TEST_PLAN_DIRECTORY)
        ), files);
    }

    @Test
    void detectsFilesAtTestPlanDirectoryAndNearestGitRoot() throws IOException {
        Path project = Files.createDirectories(tempDir.resolve("project"));
        Files.createDirectory(project.resolve(".git"));
        Path plans = Files.createDirectories(project.resolve("plans"));
        Path plan = Files.writeString(plans.resolve("checkout.jmx"), "");
        Files.writeString(plans.resolve("CLAUDE.md"), "local claude");
        Files.writeString(project.resolve("CLAUDE.md"), "root claude");
        Files.writeString(project.resolve("AGENTS.md"), "root agents");

        List<DetectedInstructionFile> files = locator.locate(plan, null);

        assertEquals(List.of(
                new DetectedInstructionFile(plans.resolve("CLAUDE.md"), InstructionFileKind.CLAUDE,
                        InstructionFileSource.TEST_PLAN_DIRECTORY),
                new DetectedInstructionFile(project.resolve("CLAUDE.md"), InstructionFileKind.CLAUDE,
                        InstructionFileSource.GIT_ROOT),
                new DetectedInstructionFile(project.resolve("AGENTS.md"), InstructionFileKind.AGENTS,
                        InstructionFileSource.GIT_ROOT)
        ), files);
    }

    @Test
    void recognizesGitWorktreeMarkerFiles() throws IOException {
        Path project = Files.createDirectories(tempDir.resolve("project"));
        Files.writeString(project.resolve(".git"), "gitdir: ../main/.git/worktrees/project");
        Path plans = Files.createDirectories(project.resolve("plans"));
        Path plan = Files.writeString(plans.resolve("checkout.jmx"), "");
        Files.writeString(project.resolve("AGENTS.md"), "root agents");

        List<DetectedInstructionFile> files = locator.locate(plan, null);

        assertEquals(List.of(
                new DetectedInstructionFile(project.resolve("AGENTS.md"), InstructionFileKind.AGENTS,
                        InstructionFileSource.GIT_ROOT)
        ), files);
    }

    @Test
    void detectsJMeterBinFilesAfterProjectFiles() throws IOException {
        Path project = Files.createDirectories(tempDir.resolve("project"));
        Path plan = Files.writeString(project.resolve("checkout.jmx"), "");
        Files.writeString(project.resolve("AGENTS.md"), "project agents");
        Path jmeterHome = Files.createDirectories(tempDir.resolve("jmeter"));
        Path bin = Files.createDirectories(jmeterHome.resolve("bin"));
        Files.writeString(bin.resolve("CLAUDE.md"), "global claude");
        Files.writeString(bin.resolve("AGENTS.md"), "global agents");

        List<DetectedInstructionFile> files = locator.locate(plan, jmeterHome);

        assertEquals(List.of(
                new DetectedInstructionFile(project.resolve("AGENTS.md"), InstructionFileKind.AGENTS,
                        InstructionFileSource.TEST_PLAN_DIRECTORY),
                new DetectedInstructionFile(bin.resolve("CLAUDE.md"), InstructionFileKind.CLAUDE,
                        InstructionFileSource.JMETER_BIN),
                new DetectedInstructionFile(bin.resolve("AGENTS.md"), InstructionFileKind.AGENTS,
                        InstructionFileSource.JMETER_BIN)
        ), files);
    }

    @Test
    void usesJMeterBinForUnsavedTestPlan() throws IOException {
        Path jmeterHome = Files.createDirectories(tempDir.resolve("jmeter"));
        Path bin = Files.createDirectories(jmeterHome.resolve("bin"));
        Files.writeString(bin.resolve("AGENTS.md"), "global agents");

        List<DetectedInstructionFile> files = locator.locate(null, jmeterHome);

        assertEquals(List.of(
                new DetectedInstructionFile(bin.resolve("AGENTS.md"), InstructionFileKind.AGENTS,
                        InstructionFileSource.JMETER_BIN)
        ), files);
        assertEquals(bin, locator.preferredCreationDirectory(null, jmeterHome));
    }

    @Test
    void doesNotReturnTheSamePhysicalDirectoryTwice() throws IOException {
        Path jmeterHome = Files.createDirectories(tempDir.resolve("jmeter"));
        Path bin = Files.createDirectories(jmeterHome.resolve("bin"));
        Path plan = Files.writeString(bin.resolve("checkout.jmx"), "");
        Files.writeString(bin.resolve("CLAUDE.md"), "claude");

        List<DetectedInstructionFile> files = locator.locate(plan, jmeterHome);

        assertEquals(List.of(
                new DetectedInstructionFile(bin.resolve("CLAUDE.md"), InstructionFileKind.CLAUDE,
                        InstructionFileSource.TEST_PLAN_DIRECTORY)
        ), files);
    }
}
