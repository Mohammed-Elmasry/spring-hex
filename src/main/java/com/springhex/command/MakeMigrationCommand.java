package com.springhex.command;

import com.springhex.generator.FileGenerator;
import com.springhex.generator.StubProcessor;
import com.springhex.util.LiquibaseChangelogUpdater;
import com.springhex.util.MigrationFileNameGenerator;
import com.springhex.util.MigrationToolDetector;
import com.springhex.util.MigrationToolDetector.MigrationTool;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Command(
    name = "make:migration",
    mixinStandardHelpOptions = true,
    description = "Generate a database migration file (Flyway or Liquibase)"
)
public class MakeMigrationCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Migration name (e.g., create_users_table)")
    private String migrationName;

    @Option(names = "--flyway", description = "Force Flyway migration")
    private boolean flyway;

    @Option(names = "--liquibase", description = "Force Liquibase migration")
    private boolean liquibase;

    @Option(names = "--format", description = "Liquibase changeset format: xml (default), yaml, sql", defaultValue = "xml")
    private String format;

    @Option(names = {"-o", "--output"}, description = "Output directory (defaults to current directory)", defaultValue = ".")
    private String outputDir;

    private final MigrationToolDetector migrationToolDetector;
    private final MigrationFileNameGenerator fileNameGenerator;
    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final LiquibaseChangelogUpdater changelogUpdater;

    public MakeMigrationCommand() {
        this.migrationToolDetector = new MigrationToolDetector();
        this.fileNameGenerator = new MigrationFileNameGenerator();
        this.stubProcessor = new StubProcessor();
        this.fileGenerator = new FileGenerator();
        this.changelogUpdater = new LiquibaseChangelogUpdater();
    }

    @Override
    public Integer call() {
        if (flyway && liquibase) {
            System.err.println("Error: --flyway and --liquibase are mutually exclusive.");
            return 1;
        }

        MigrationTool tool;
        if (flyway) {
            tool = MigrationTool.FLYWAY;
        } else if (liquibase) {
            tool = MigrationTool.LIQUIBASE;
        } else {
            tool = migrationToolDetector.detect(outputDir);
            if (tool == null) {
                System.err.println("Error: Could not auto-detect migration tool. Use --flyway or --liquibase.");
                return 1;
            }
            System.out.println("Auto-detected migration tool: " + tool);
        }

        try {
            if (tool == MigrationTool.FLYWAY) {
                return generateFlywayMigration();
            } else {
                return generateLiquibaseMigration();
            }
        } catch (IOException e) {
            System.err.println("Error generating migration: " + e.getMessage());
            return 1;
        }
    }

    private int generateFlywayMigration() throws IOException {
        String fileName = fileNameGenerator.generateFlywayFileName(migrationName);
        Path migrationDir = Paths.get(outputDir, "src/main/resources/db/migration");
        Path outputPath = migrationDir.resolve(fileName);

        boolean isFirstMigration = !Files.exists(migrationDir) || isDirectoryEmpty(migrationDir);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{{MIGRATION_NAME}}", migrationName);
        replacements.put("{{TIMESTAMP}}", timestamp);

        String content = stubProcessor.process("migration/flyway-sql", replacements);
        fileGenerator.generate(outputPath, content);
        System.out.println("Created: " + outputPath);

        if (isFirstMigration && migrationToolDetector.shouldWarnFlywayOutOfOrder(outputDir)) {
            System.out.println();
            System.out.println("WARNING: spring.flyway.out-of-order=true is not set in application.properties.");
            System.out.println("If working with multiple branches, consider adding it to avoid migration ordering issues.");
        }

        return 0;
    }

    private int generateLiquibaseMigration() throws IOException {
        String validFormats = "xml,yaml,sql";
        if (!validFormats.contains(format.toLowerCase())) {
            System.err.println("Error: Invalid format '" + format + "'. Supported: xml, yaml, sql");
            return 1;
        }
        String fmt = format.toLowerCase();

        String fileName = fileNameGenerator.generateLiquibaseFileName(migrationName, fmt);
        Path changesDir = Paths.get(outputDir, "src/main/resources/db/changelog/changes");
        Path outputPath = changesDir.resolve(fileName);

        String changesetId = migrationName.toLowerCase().replace(' ', '_').replace('-', '_');
        Map<String, String> replacements = new HashMap<>();
        replacements.put("{{CHANGESET_ID}}", changesetId);

        String stubName = "migration/liquibase-changeset-" + fmt;
        String content = stubProcessor.process(stubName, replacements);
        fileGenerator.generate(outputPath, content);
        System.out.println("Created: " + outputPath);

        // Ensure master changelog exists and add include
        ensureMasterChangelog(fmt, fileName);

        return 0;
    }

    private void ensureMasterChangelog(String format, String changesetFileName) throws IOException {
        boolean useYamlMaster = format.equals("yaml");
        String masterFileName = useYamlMaster ? "db.changelog-master.yaml" : "db.changelog-master.xml";
        String masterStub = useYamlMaster ? "migration/liquibase-master-yaml" : "migration/liquibase-master-xml";

        Path changelogDir = Paths.get(outputDir, "src/main/resources/db/changelog");
        Path masterPath = changelogDir.resolve(masterFileName);

        if (!Files.exists(masterPath)) {
            String masterContent = stubProcessor.process(masterStub, Map.of());
            fileGenerator.generate(masterPath, masterContent);
            System.out.println("Created: " + masterPath);
        }

        String relativePath = "changes/" + changesetFileName;

        if (useYamlMaster) {
            changelogUpdater.addIncludeToYamlChangelog(masterPath, relativePath);
        } else {
            changelogUpdater.addIncludeToXmlChangelog(masterPath, relativePath);
        }
        System.out.println("Updated: " + masterPath + " (added include for " + changesetFileName + ")");
    }

    private boolean isDirectoryEmpty(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) return true;
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.noneMatch(p -> p.toString().endsWith(".sql"));
        }
    }
}
