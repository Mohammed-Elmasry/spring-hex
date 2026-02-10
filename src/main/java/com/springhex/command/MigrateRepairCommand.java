package com.springhex.command;

import com.springhex.util.BuildToolDetector;
import com.springhex.util.BuildToolDetector.BuildTool;
import com.springhex.util.MigrationToolDetector;
import com.springhex.util.MigrationToolDetector.MigrationTool;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "migrate:repair",
    mixinStandardHelpOptions = true,
    description = "Repair database migration metadata"
)
public class MigrateRepairCommand implements Callable<Integer> {

    private final BuildToolDetector buildToolDetector;
    private final MigrationToolDetector migrationToolDetector;

    public MigrateRepairCommand() {
        this.buildToolDetector = new BuildToolDetector();
        this.migrationToolDetector = new MigrationToolDetector();
    }

    @Override
    public Integer call() {
        String baseDir = System.getProperty("user.dir");

        BuildTool buildTool = buildToolDetector.detect(baseDir);
        if (buildTool == null) {
            System.err.println("Error: No build tool detected. Ensure you are in a Maven or Gradle project directory.");
            return 1;
        }

        MigrationTool migrationTool = migrationToolDetector.detect(baseDir);
        if (migrationTool == null) {
            System.err.println("Error: No migration tool detected. Ensure Flyway or Liquibase is configured.");
            return 1;
        }

        String executable = buildToolDetector.resolveExecutable(baseDir, buildTool);
        List<String> command = buildCommand(executable, buildTool, migrationTool);

        System.out.println("Detected: " + buildTool + " + " + migrationTool);
        System.out.println("Running: " + String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(baseDir));
            pb.inheritIO();
            Process process = pb.start();
            return process.waitFor();
        } catch (IOException e) {
            System.err.println("Error repairing migrations: " + e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Repair interrupted.");
            return 1;
        }
    }

    private List<String> buildCommand(String executable, BuildTool buildTool, MigrationTool migrationTool) {
        List<String> command = new ArrayList<>();
        command.add(executable);

        if (migrationTool == MigrationTool.FLYWAY) {
            command.add(buildTool == BuildTool.MAVEN ? "flyway:repair" : "flywayRepair");
        } else {
            command.add(buildTool == BuildTool.MAVEN ? "liquibase:changelogSync" : "changelogSync");
        }

        return command;
    }
}
