package com.springhex.command;

import com.springhex.config.ConfigResolver;
import com.springhex.config.ConfigurationException;
import com.springhex.config.HexPathResolver;
import com.springhex.config.ResolvedConfig;
import com.springhex.generator.FileGenerator;
import com.springhex.generator.StubProcessor;
import com.springhex.util.PackageResolver;
import com.springhex.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:entity",
    mixinStandardHelpOptions = true,
    description = "Generate domain entity and JPA entity"
)
public class MakeEntityCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Entity name (e.g., Order)")
    private String entityName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Option(names = {"--table"}, description = "Database table name (defaults to plural of aggregate)")
    private String tableName;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeEntityCommand() {
        this.stubProcessor = new StubProcessor();
        this.fileGenerator = new FileGenerator();
        this.packageResolver = new PackageResolver();
    }

    @Override
    public Integer call() {
        try {
            ResolvedConfig config = ConfigResolver.resolve(mixin.getOutputDir(), mixin.getBasePackage());
            String resolvedPackage = config.getBasePackage();
            HexPathResolver pathResolver = config.getPathResolver();

            String entity = StringUtils.capitalize(entityName);
            String aggregateLower = aggregate.toLowerCase();
            String table = tableName != null ? tableName : StringUtils.pluralize(aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{ENTITY_NAME}}", entity);
            replacements.put("{{TABLE_NAME}}", table);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // 1. Generate Domain Entity
            String domainPackage = pathResolver.resolve("model", aggregateLower);
            generateFile("domain/entity", entity, domainPackage, replacements);

            // 2. Generate JPA Entity
            String infraPackage = pathResolver.resolve("persistence", aggregateLower);
            generateFile("infrastructure/jpa-entity", entity + "JpaEntity", infraPackage, replacements);

            System.out.println("\nEntities generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating entity: " + e.getMessage());
            return 1;
        }
    }

    private void generateFile(String stubName, String className, String packageName, Map<String, String> replacements) throws IOException {
        Map<String, String> fileReplacements = new HashMap<>(replacements);
        fileReplacements.put("{{PACKAGE}}", packageName);
        String content = stubProcessor.process(stubName, fileReplacements);
        Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), className, packageName);
        fileGenerator.generate(outputPath, content);
        System.out.println("Created: " + outputPath);
    }
}
