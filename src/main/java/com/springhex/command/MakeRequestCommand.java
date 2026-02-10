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
    name = "make:request",
    mixinStandardHelpOptions = true,
    description = "Generate a request DTO"
)
public class MakeRequestCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Request name (e.g., CreateOrderRequest)")
    private String requestName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeRequestCommand() {
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

            String className = normalizeRequestName(requestName);
            String aggregateLower = aggregate.toLowerCase();

            String dtoPackage = pathResolver.resolve("dto", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{REQUEST_NAME}}", className);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            String content = stubProcessor.process("domain/request", replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), className, dtoPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            System.out.println("\nRequest DTO generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating request: " + e.getMessage());
            return 1;
        }
    }

    private String normalizeRequestName(String name) {
        String capitalized = StringUtils.capitalize(name);
        if (!capitalized.endsWith("Request")) {
            return capitalized + "Request";
        }
        return capitalized;
    }
}
