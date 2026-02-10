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
    name = "make:event",
    mixinStandardHelpOptions = true,
    description = "Generate a domain event and optional event listener"
)
public class MakeEventCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Event name (e.g., OrderCreated; auto-appends 'Event' suffix if missing)")
    private String eventName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Option(names = {"--no-listener"}, description = "Skip generating the event listener", defaultValue = "false")
    private boolean noListener;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeEventCommand() {
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

            String className = normalizeEventName(eventName);
            String aggregateLower = aggregate.toLowerCase();

            String eventPackage = pathResolver.resolve("event", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{EVENT_NAME}}", className);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // Generate domain event record
            String eventContent = stubProcessor.process("domain/domain-event", replacements);
            Path eventPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), className, eventPackage);
            fileGenerator.generate(eventPath, eventContent);
            System.out.println("Created: " + eventPath);

            // Generate event listener unless --no-listener
            if (!noListener) {
                String listenerPackage = pathResolver.resolve("event-listener", aggregateLower);
                String listenerClassName = className + "Listener";

                String listenerContent = stubProcessor.process("infrastructure/event-listener", replacements);
                Path listenerPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), listenerClassName, listenerPackage);
                fileGenerator.generate(listenerPath, listenerContent);
                System.out.println("Created: " + listenerPath);
            }

            System.out.println("\nEvent generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating event: " + e.getMessage());
            return 1;
        }
    }

    private String normalizeEventName(String name) {
        String capitalized = StringUtils.capitalize(name);
        if (!capitalized.endsWith("Event")) {
            return capitalized + "Event";
        }
        return capitalized;
    }
}
