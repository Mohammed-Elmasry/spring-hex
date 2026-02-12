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
    name = "make:port",
    mixinStandardHelpOptions = true,
    description = "Generate a hexagonal architecture port interface (input or output)"
)
public class MakePortCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Port name (e.g., NotificationSender, PlaceOrderUseCase)")
    private String portName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Option(names = {"--in"}, description = "Generate as input port (default is output port)", defaultValue = "false")
    private boolean inputPort;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakePortCommand() {
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

            String className = StringUtils.capitalize(portName);
            String aggregateLower = aggregate.toLowerCase();

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{PORT_NAME}}", className);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            String stubName;
            String portPackage;

            if (inputPort) {
                stubName = "domain/input-port";
                portPackage = pathResolver.resolve("port-in", aggregateLower);
            } else {
                stubName = "domain/output-port";
                portPackage = pathResolver.resolve("port-out", aggregateLower);
            }
            replacements.put("{{PACKAGE}}", portPackage);

            String content = stubProcessor.process(stubName, replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), className, portPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            String portType = inputPort ? "input" : "output";
            System.out.println("\n" + StringUtils.capitalize(portType) + " port generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating port: " + e.getMessage());
            return 1;
        }
    }
}
