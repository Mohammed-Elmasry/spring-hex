package com.springhex.command;

import com.springhex.util.BuildToolDetector;
import com.springhex.util.BuildToolDetector.BuildTool;
import com.springhex.util.MigrationToolDetector;
import com.springhex.util.MigrationToolDetector.MigrationTool;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
    name = "migrate:fresh",
    mixinStandardHelpOptions = true,
    description = "Drop all tables and re-run all migrations (destructive!)"
)
public class MigrateFreshCommand implements Callable<Integer> {

    @Option(names = "--force", required = true, description = "Required flag to confirm destructive operation")
    private boolean force;

    private final BuildToolDetector buildToolDetector;
    private final MigrationToolDetector migrationToolDetector;

    public MigrateFreshCommand() {
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

        System.out.println("Detected: " + buildTool + " + " + migrationTool);

        // Step 1: Clean/Drop all
        List<String> cleanCommand = buildCleanCommand(executable, buildTool, migrationTool);
        System.out.println("Step 1 - Cleaning: " + String.join(" ", cleanCommand));

        int cleanResult = executeCommand(cleanCommand, baseDir);
        if (cleanResult != 0) {
            System.err.println("Error: Clean step failed with exit code " + cleanResult + ". Aborting.");
            return cleanResult;
        }

        // Step 2: Migrate
        List<String> migrateCommand = buildMigrateCommand(executable, buildTool, migrationTool);
        System.out.println("Step 2 - Migrating: " + String.join(" ", migrateCommand));

        int migrateResult = executeCommand(migrateCommand, baseDir);
        if (migrateResult != 0) {
            System.err.println("Error: Migrate step failed with exit code " + migrateResult);
            return migrateResult;
        }

        System.out.println("Fresh migration completed successfully!");
        return 0;
    }

    private int executeCommand(List<String> command, String baseDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(baseDir));
            pb.inheritIO();
            Process process = pb.start();
            return process.waitFor();
        } catch (IOException e) {
            System.err.println("Error executing command: " + e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Command execution interrupted.");
            return 1;
        }
    }

    private List<String> buildCleanCommand(String executable, BuildTool buildTool, MigrationTool migrationTool) {
        List<String> command = new ArrayList<>();
        command.add(executable);

        if (migrationTool == MigrationTool.FLYWAY) {
            command.add(buildTool == BuildTool.MAVEN ? "flyway:clean" : "flywayClean");
        } else {
            command.add(buildTool == BuildTool.MAVEN ? "liquibase:dropAll" : "dropAll");
        }

        return command;
    }

    private List<String> buildMigrateCommand(String executable, BuildTool buildTool, MigrationTool migrationTool) {
        List<String> command = new ArrayList<>();
        command.add(executable);

        if (migrationTool == MigrationTool.FLYWAY) {
            command.add(buildTool == BuildTool.MAVEN ? "flyway:migrate" : "flywayMigrate");
        } else {
            command.add(buildTool == BuildTool.MAVEN ? "liquibase:update" : "update");
        }

        return command;
    }
}
