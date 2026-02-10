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
    name = "make:value-object",
    mixinStandardHelpOptions = true,
    description = "Generate a DDD value object (Java record)"
)
public class MakeValueObjectCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Value object name (e.g., Money, OrderId)")
    private String valueObjectName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeValueObjectCommand() {
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

            String className = StringUtils.capitalize(valueObjectName);
            String aggregateLower = aggregate.toLowerCase();

            String modelPackage = pathResolver.resolve("model", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{VALUE_OBJECT_NAME}}", className);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // Auto-detect ID pattern: use value-object-id.stub if name ends with "Id"
            String stubName = className.endsWith("Id") ? "domain/value-object-id" : "domain/value-object";

            String content = stubProcessor.process(stubName, replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), className, modelPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            System.out.println("\nValue object generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating value object: " + e.getMessage());
            return 1;
        }
    }
}
