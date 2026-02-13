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
    name = "make:seeder",
    mixinStandardHelpOptions = true,
    description = "Generate a database seeder class"
)
public class MakeSeederCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Seeder name (e.g., UserSeeder)")
    private String seederName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (defaults to entity name lowercase)")
    private String aggregate;

    @Option(names = {"--entity"}, description = "Entity name for factory/repository imports (e.g., User)", required = true)
    private String entityName;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeSeederCommand() {
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

            String className = normalizeSeederName(seederName);
            String entityCapitalized = StringUtils.capitalize(entityName);
            String aggregateLower = (aggregate != null ? aggregate : stripEntitySuffix(entityName)).toLowerCase();

            String seederPackage = pathResolver.resolveStatic("seeder");
            String factoryPackage = pathResolver.resolve("factory", aggregateLower);
            String repositoryPackage = pathResolver.resolve("persistence", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{PACKAGE}}", seederPackage);
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{SEEDER_NAME}}", className);
            replacements.put("{{ENTITY_NAME}}", entityCapitalized);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{PACKAGE_FACTORY}}", factoryPackage);
            replacements.put("{{PACKAGE_REPOSITORY}}", repositoryPackage);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // Generate seeder class
            String content = stubProcessor.process("data/seeder", replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), className, seederPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            // Auto-generate Seeder interface if it doesn't exist
            Path seederInterfacePath = packageResolver.resolveOutputPath(mixin.getOutputDir(), "Seeder", seederPackage);
            if (!Files.exists(seederInterfacePath)) {
                Map<String, String> interfaceReplacements = new HashMap<>();
                interfaceReplacements.put("{{PACKAGE}}", seederPackage);

                String interfaceContent = stubProcessor.process("data/seeder-interface", interfaceReplacements);
                fileGenerator.generate(seederInterfacePath, interfaceContent);
                System.out.println("Created: " + seederInterfacePath);
            }

            // Auto-generate SeedRunner if it doesn't exist
            Path seedRunnerPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), "SeedRunner", seederPackage);
            if (!Files.exists(seedRunnerPath)) {
                Map<String, String> runnerReplacements = new HashMap<>();
                runnerReplacements.put("{{PACKAGE}}", seederPackage);

                String runnerContent = stubProcessor.process("data/seed-runner", runnerReplacements);
                fileGenerator.generate(seedRunnerPath, runnerContent);
                System.out.println("Created: " + seedRunnerPath);
            }

            System.out.println("\nSeeder generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating seeder: " + e.getMessage());
            return 1;
        }
    }

    private String stripEntitySuffix(String name) {
        if (name.endsWith("Entity")) {
            return name.substring(0, name.length() - "Entity".length());
        }
        return name;
    }

    private String normalizeSeederName(String name) {
        String capitalized = StringUtils.capitalize(name);
        if (!capitalized.endsWith("Seeder")) {
            return capitalized + "Seeder";
        }
        return capitalized;
    }
}
