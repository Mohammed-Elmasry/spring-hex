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
    name = "make:repository",
    mixinStandardHelpOptions = true,
    description = "Generate repository port and data store adapter (supports jpa, mongodb, redis)"
)
public class MakeRepositoryCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Entity name (e.g., Order)")
    private String entityName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Option(names = {"-s", "--store"}, description = "Data store type: jpa (default), mongodb, redis", defaultValue = "jpa")
    private String store;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeRepositoryCommand() {
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
            String aggregateCapitalized = StringUtils.capitalize(aggregate);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{AGGREGATE_CAPITALIZED}}", aggregateCapitalized);
            replacements.put("{{ENTITY_NAME}}", entity);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // 1. Generate Repository Port (domain layer)
            String portPackage = pathResolver.resolve("port-out", aggregateLower);
            generateFile("domain/repository-port", aggregateCapitalized + "Repository", portPackage, replacements);

            // 2+. Generate infrastructure layer (varies by store)
            String infraPackage = pathResolver.resolve("persistence", aggregateLower);
            String storeLower = store.toLowerCase();

            switch (storeLower) {
                case "jpa" -> {
                    generateFile("infrastructure/spring-data-repository", aggregateCapitalized + "JpaRepository", infraPackage, replacements);
                    generateFile("infrastructure/repository-adapter", aggregateCapitalized + "RepositoryAdapter", infraPackage, replacements);
                }
                case "mongodb" -> {
                    generateFile("infrastructure/mongo-document", aggregateCapitalized + "MongoDocument", infraPackage, replacements);
                    generateFile("infrastructure/spring-data-mongo-repository", aggregateCapitalized + "MongoRepository", infraPackage, replacements);
                    generateFile("infrastructure/mongo-repository-adapter", aggregateCapitalized + "RepositoryAdapter", infraPackage, replacements);
                }
                case "redis" -> {
                    generateFile("infrastructure/redis-hash-entity", aggregateCapitalized + "RedisEntity", infraPackage, replacements);
                    generateFile("infrastructure/redis-repository", aggregateCapitalized + "RedisRepository", infraPackage, replacements);
                    generateFile("infrastructure/redis-repository-adapter", aggregateCapitalized + "RepositoryAdapter", infraPackage, replacements);
                }
                default -> {
                    System.err.println("Error: Unknown store type '" + store + "'. Supported: jpa, mongodb, redis");
                    return 1;
                }
            }

            System.out.println("\nRepository layer generated successfully! (store: " + storeLower + ")");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating repository: " + e.getMessage());
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
