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
    name = "make:adapter",
    mixinStandardHelpOptions = true,
    description = "Generate a hexagonal architecture adapter implementation"
)
public class MakeAdapterCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Adapter name (e.g., SmtpNotificationSender)")
    private String adapterName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Option(names = {"--port"}, description = "Port interface name to implement (e.g., NotificationSender)", required = true)
    private String portName;

    @Option(names = {"--category"}, description = "Infrastructure subdirectory (e.g., notification, payment); defaults to aggregate name")
    private String category;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeAdapterCommand() {
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

            String className = StringUtils.capitalize(adapterName);
            String aggregateLower = aggregate.toLowerCase();
            String portClassName = StringUtils.capitalize(portName);
            String adapterCategory = (category != null && !category.isBlank()) ? category.toLowerCase() : aggregateLower;

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{PORT_NAME}}", portClassName);
            replacements.put("{{ADAPTER_NAME}}", className);
            replacements.put("{{ADAPTER_CATEGORY}}", adapterCategory);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            String adapterPackage = pathResolver.resolve("adapter", java.util.Map.of("aggregate", aggregateLower, "category", adapterCategory));

            String content = stubProcessor.process("infrastructure/adapter", replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), className, adapterPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            System.out.println("\nAdapter generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating adapter: " + e.getMessage());
            return 1;
        }
    }
}
