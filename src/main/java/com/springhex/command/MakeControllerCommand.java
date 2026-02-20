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
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:controller",
    mixinStandardHelpOptions = true,
    description = "Generate a REST controller"
)
public class MakeControllerCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Aggregate name (e.g., order)")
    private String aggregate;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeControllerCommand() {
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

            // Strip "Controller" suffix before deriving aggregate name so that
            // both "order" and "OrderController" produce "OrderController".
            String aggregateBase = stripControllerSuffix(aggregate);
            String aggregateLower = aggregateBase.toLowerCase();
            String aggregateCapitalized = StringUtils.capitalize(aggregateBase);
            String aggregatePlural = StringUtils.pluralize(aggregateLower);

            String controllerPackage = pathResolver.resolve("controller", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{PACKAGE}}", controllerPackage);
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{AGGREGATE_CAPITALIZED}}", aggregateCapitalized);
            replacements.put("{{AGGREGATE_PLURAL}}", aggregatePlural);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            String content = stubProcessor.process("infrastructure/controller", replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), aggregateCapitalized + "Controller", controllerPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            System.out.println("\nController generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating controller: " + e.getMessage());
            return 1;
        }
    }

    private String stripControllerSuffix(String name) {
        String capitalized = StringUtils.capitalize(name);
        if (capitalized.endsWith("Controller")) {
            return capitalized.substring(0, capitalized.length() - "Controller".length());
        }
        return capitalized;
    }
}
