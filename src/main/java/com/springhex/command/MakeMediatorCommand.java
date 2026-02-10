package com.springhex.command;

import com.springhex.generator.FileGenerator;
import com.springhex.generator.StubProcessor;
import com.springhex.util.PackageResolver;
import com.springhex.config.HexPathResolver;
import com.springhex.config.ConfigResolver;
import com.springhex.config.ResolvedConfig;
import com.springhex.config.ConfigurationException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:mediator",
    mixinStandardHelpOptions = true,
    description = "Generate mediator pattern infrastructure (CommandBus, QueryBus, handlers, DomainConfig)"
)
public class MakeMediatorCommand implements Callable<Integer> {

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeMediatorCommand() {
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

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{PACKAGE_CQRS}}", pathResolver.resolveStatic("cqrs"));
            replacements.put("{{PACKAGE_MEDIATOR}}", pathResolver.resolveStatic("mediator"));

            String mediatorPackage = pathResolver.resolveStatic("mediator");
            String configPackage = pathResolver.resolveStatic("config");
            String cqrsPackage = pathResolver.resolveStatic("cqrs");

            // Generate CommandBus interface
            generateFile("mediator/CommandBus", "CommandBus", mediatorPackage, replacements);

            // Generate SimpleCommandBus implementation
            generateFile("mediator/SimpleCommandBus", "SimpleCommandBus", mediatorPackage, replacements);

            // Generate QueryBus interface
            generateFile("mediator/QueryBus", "QueryBus", mediatorPackage, replacements);

            // Generate SimpleQueryBus implementation
            generateFile("mediator/SimpleQueryBus", "SimpleQueryBus", mediatorPackage, replacements);

            // Generate MediatorConfig
            generateFile("mediator/MediatorConfig", "MediatorConfig", configPackage, replacements);

            // Generate CommandHandler interface in domain.cqrs
            generateFile("domain/command-handler-interface", "CommandHandler", cqrsPackage, replacements);

            // Generate QueryHandler interface in domain.cqrs
            generateFile("domain/query-handler-interface", "QueryHandler", cqrsPackage, replacements);

            // Generate DomainConfig
            generateFile("infrastructure/domain-config", "DomainConfig", configPackage, replacements);

            // Generate AggregateRoot base class
            String domainPackage = pathResolver.resolveStatic("domain-root");
            generateFile("domain/aggregate-root", "AggregateRoot", domainPackage, replacements);

            System.out.println("\nMediator infrastructure generated successfully!");
            System.out.println("Generated 9 files in " + resolvedPackage);
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating mediator files: " + e.getMessage());
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
