package com.springhex.command;

import com.springhex.util.BuildToolDetector;
import com.springhex.util.BuildToolDetector.BuildTool;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "run:test",
    mixinStandardHelpOptions = true,
    description = "Run tests using the project's build tool (Maven or Gradle)"
)
public class RunTestCommand implements Callable<Integer> {

    @Option(names = "--unit", description = "Run only unit tests")
    private boolean unit;

    @Option(names = "--feature", description = "Run only feature tests")
    private boolean feature;

    private final BuildToolDetector buildToolDetector;

    public RunTestCommand() {
        this.buildToolDetector = new BuildToolDetector();
    }

    @Override
    public Integer call() {
        String baseDir = System.getProperty("user.dir");

        BuildTool tool = buildToolDetector.detect(baseDir);
        if (tool == null) {
            System.err.println("Error: No build tool detected. Ensure you are in a Maven or Gradle project directory.");
            return 1;
        }

        String executable = buildToolDetector.resolveExecutable(baseDir, tool);
        List<String> command = buildCommand(executable, tool);

        System.out.println("Detected build tool: " + tool);
        System.out.println("Running: " + String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(baseDir));
            pb.inheritIO();
            Process process = pb.start();
            return process.waitFor();
        } catch (IOException e) {
            System.err.println("Error executing tests: " + e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Test execution interrupted.");
            return 1;
        }
    }

    private List<String> buildCommand(String executable, BuildTool tool) {
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("test");

        if (unit) {
            if (tool == BuildTool.MAVEN) {
                command.add("-Dtest=**/unit/**");
            } else {
                command.add("--tests");
                command.add("*.unit.*");
            }
        } else if (feature) {
            if (tool == BuildTool.MAVEN) {
                command.add("-Dtest=**/feature/**");
            } else {
                command.add("--tests");
                command.add("*.feature.*");
            }
        }

        return command;
    }
}
