package com.springhex.command;

import com.springhex.generator.FileGenerator;
import com.springhex.generator.StubProcessor;
import com.springhex.util.BuildToolDetector;
import com.springhex.util.BuildToolDetector.BuildTool;
import com.springhex.util.MigrationFileNameGenerator;
import com.springhex.util.MigrationToolDetector;
import com.springhex.util.MigrationToolDetector.MigrationTool;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Command(
    name = "migrate:rollback",
    mixinStandardHelpOptions = true,
    description = "Rollback database migrations"
)
public class MigrateRollbackCommand implements Callable<Integer> {

    @Option(names = "--step", description = "Number of changesets to rollback (Liquibase only)", defaultValue = "1")
    private int step;

    private final BuildToolDetector buildToolDetector;
    private final MigrationToolDetector migrationToolDetector;
    private final MigrationFileNameGenerator fileNameGenerator;
    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;

    public MigrateRollbackCommand() {
        this.buildToolDetector = new BuildToolDetector();
        this.migrationToolDetector = new MigrationToolDetector();
        this.fileNameGenerator = new MigrationFileNameGenerator();
        this.stubProcessor = new StubProcessor();
        this.fileGenerator = new FileGenerator();
    }

    @Override
    public Integer call() {
        String baseDir = System.getProperty("user.dir");

        MigrationTool migrationTool = migrationToolDetector.detect(baseDir);
        if (migrationTool == null) {
            System.err.println("Error: No migration tool detected. Ensure Flyway or Liquibase is configured.");
            return 1;
        }

        if (migrationTool == MigrationTool.FLYWAY) {
            return handleFlywayRollback(baseDir);
        } else {
            return handleLiquibaseRollback(baseDir);
        }
    }

    private int handleFlywayRollback(String baseDir) {
        Path migrationDir = Paths.get(baseDir, "src/main/resources/db/migration");
        if (!Files.isDirectory(migrationDir)) {
            System.err.println("Error: No migration directory found at " + migrationDir);
            return 1;
        }

        try {
            Optional<Path> lastMigration = findLastFlywayMigration(migrationDir);
            if (lastMigration.isEmpty()) {
                System.err.println("Error: No Flyway migration files found in " + migrationDir);
                return 1;
            }

            String originalFileName = lastMigration.get().getFileName().toString();
            String originalMigrationName = extractMigrationName(originalFileName);

            String revertFileName = fileNameGenerator.generateFlywayRevertFileName(originalMigrationName);
            Path revertPath = migrationDir.resolve(revertFileName);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{ORIGINAL_MIGRATION}}", originalMigrationName);
            replacements.put("{{ORIGINAL_FILE}}", originalFileName);

            String content = stubProcessor.process("migration/flyway-revert-sql", replacements);
            fileGenerator.generate(revertPath, content);

            System.out.println("Created: " + revertPath);
            System.out.println();
            System.out.println("Flyway Community Edition doesn't support undo migrations.");
            System.out.println("A revert migration has been created. Edit it with the appropriate rollback SQL,");
            System.out.println("then run: spring-hex migrate");

            return 0;
        } catch (IOException e) {
            System.err.println("Error generating revert migration: " + e.getMessage());
            return 1;
        }
    }

    private int handleLiquibaseRollback(String baseDir) {
        BuildTool buildTool = buildToolDetector.detect(baseDir);
        if (buildTool == null) {
            System.err.println("Error: No build tool detected. Ensure you are in a Maven or Gradle project directory.");
            return 1;
        }

        String executable = buildToolDetector.resolveExecutable(baseDir, buildTool);
        List<String> command = buildLiquibaseRollbackCommand(executable, buildTool);

        System.out.println("Detected: " + buildTool + " + LIQUIBASE");
        System.out.println("Running: " + String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(baseDir));
            pb.inheritIO();
            Process process = pb.start();
            return process.waitFor();
        } catch (IOException e) {
            System.err.println("Error executing rollback: " + e.getMessage());
            return 1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Rollback execution interrupted.");
            return 1;
        }
    }

    private List<String> buildLiquibaseRollbackCommand(String executable, BuildTool buildTool) {
        List<String> command = new ArrayList<>();
        command.add(executable);

        if (buildTool == BuildTool.MAVEN) {
            command.add("liquibase:rollback");
            command.add("-Dliquibase.rollbackCount=" + step);
        } else {
            command.add("rollbackCount");
            command.add("-PliquibaseCommandValue=" + step);
        }

        return command;
    }

    private Optional<Path> findLastFlywayMigration(Path migrationDir) throws IOException {
        try (Stream<Path> files = Files.list(migrationDir)) {
            return files
                .filter(p -> p.getFileName().toString().startsWith("V"))
                .filter(p -> p.getFileName().toString().endsWith(".sql"))
                .max(Comparator.comparing(p -> p.getFileName().toString()));
        }
    }

    private String extractMigrationName(String fileName) {
        // V20260207143025123__create_users_table.sql → create_users_table
        int doubleUnderscore = fileName.indexOf("__");
        if (doubleUnderscore == -1) return fileName;
        String withExtension = fileName.substring(doubleUnderscore + 2);
        if (withExtension.endsWith(".sql")) {
            return withExtension.substring(0, withExtension.length() - 4);
        }
        return withExtension;
    }
}
