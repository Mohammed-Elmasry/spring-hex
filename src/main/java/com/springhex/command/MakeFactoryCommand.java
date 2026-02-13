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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:factory",
    mixinStandardHelpOptions = true,
    description = "Generate a data factory class using Datafaker"
)
public class MakeFactoryCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Entity name (e.g., User, Product)")
    private String entityName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (defaults to entity name lowercase)")
    private String aggregate;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeFactoryCommand() {
        this.stubProcessor = new StubProcessor();
        this.fileGenerator = new FileGenerator();
        this.packageResolver = new PackageResolver();
    }

    private String stripEntitySuffix(String name) {
        if (name.endsWith("Entity")) {
            return name.substring(0, name.length() - "Entity".length());
        }
        return name;
    }

    @Override
    public Integer call() {
        try {
            ResolvedConfig config = ConfigResolver.resolve(mixin.getOutputDir(), mixin.getBasePackage());
            String resolvedPackage = config.getBasePackage();
            HexPathResolver pathResolver = config.getPathResolver();

            String capitalized = StringUtils.capitalize(entityName);
            String aggregateLower = (aggregate != null ? aggregate : stripEntitySuffix(entityName)).toLowerCase();

            String factoryPackage = pathResolver.resolve("factory", aggregateLower);
            String repositoryPackage = pathResolver.resolve("persistence", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{PACKAGE}}", factoryPackage);
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{ENTITY_NAME}}", capitalized);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{PACKAGE_REPOSITORY}}", repositoryPackage);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // Generate factory class
            String content = stubProcessor.process("data/factory", replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), capitalized + "Factory", factoryPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            // Auto-generate repository if it doesn't exist
            Path repoPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), capitalized + "Repository", repositoryPackage);
            if (!Files.exists(repoPath)) {
                Map<String, String> repoReplacements = new HashMap<>();
                repoReplacements.put("{{PACKAGE}}", repositoryPackage);
                repoReplacements.put("{{ENTITY_NAME}}", capitalized);

                String repoContent = stubProcessor.process("data/repository", repoReplacements);
                fileGenerator.generate(repoPath, repoContent);
                System.out.println("Created: " + repoPath);
            }

            System.out.println("\nFactory generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating factory: " + e.getMessage());
            return 1;
        }
    }
}
