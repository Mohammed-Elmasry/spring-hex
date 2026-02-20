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
    name = "make:mapper",
    mixinStandardHelpOptions = true,
    description = "Generate bi-directional mapper between domain and JPA entity"
)
public class MakeMapperCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Entity name (e.g., Order)")
    private String entityName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeMapperCommand() {
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

            // Strip "Mapper" suffix so that both "Order" and "OrderMapper" produce
            // the same output: an OrderMapper class backed by {{ENTITY_NAME}}Mapper in the stub.
            String entity = stripMapperSuffix(entityName);
            String aggregateLower = aggregate.toLowerCase();

            String mapperPackage = pathResolver.resolve("persistence", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{PACKAGE}}", mapperPackage);
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{ENTITY_NAME}}", entity);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            String content = stubProcessor.process("infrastructure/mapper", replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), entity + "Mapper", mapperPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            System.out.println("\nMapper generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating mapper: " + e.getMessage());
            return 1;
        }
    }

    private String stripMapperSuffix(String name) {
        String capitalized = StringUtils.capitalize(name);
        if (capitalized.endsWith("Mapper")) {
            return capitalized.substring(0, capitalized.length() - "Mapper".length());
        }
        return capitalized;
    }
}
