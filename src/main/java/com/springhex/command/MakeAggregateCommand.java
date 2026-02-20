package com.springhex.command;

import com.springhex.config.ConfigResolver;
import com.springhex.config.ConfigurationException;
import com.springhex.config.HexPathResolver;
import com.springhex.config.ResolvedConfig;
import com.springhex.generator.ConfigAppender;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:aggregate",
    mixinStandardHelpOptions = true,
    description = "Generate a DDD aggregate root class"
)
public class MakeAggregateCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Aggregate name (e.g., Order)")
    private String aggregateName;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;
    private final ConfigAppender configAppender;

    public MakeAggregateCommand() {
        this.stubProcessor = new StubProcessor();
        this.fileGenerator = new FileGenerator();
        this.packageResolver = new PackageResolver();
        this.configAppender = new ConfigAppender();
    }

    @Override
    public Integer call() {
        try {
            ResolvedConfig config = ConfigResolver.resolve(mixin.getOutputDir(), mixin.getBasePackage());
            String resolvedPackage = config.getBasePackage();
            HexPathResolver pathResolver = config.getPathResolver();

            String aggregateCapitalized = StringUtils.capitalize(aggregateName);
            String aggregateLower = aggregateName.toLowerCase();

            String modelPackage = pathResolver.resolve("model", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{PACKAGE}}", modelPackage);
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{AGGREGATE_CAPITALIZED}}", aggregateCapitalized);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // Generate Aggregate class
            String content = stubProcessor.process("domain/aggregate", replacements);
            Path outputPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), aggregateCapitalized, modelPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            // Register @Bean in DomainConfig if it exists
            String configPackage = pathResolver.resolveStatic("config");
            String beanName = aggregateLower;
            Map<String, String> beanReplacements = new HashMap<>();
            beanReplacements.put("{{CLASS_NAME}}", aggregateCapitalized);
            beanReplacements.put("{{BEAN_NAME}}", beanName);
            List<String> imports = List.of(
                modelPackage + "." + aggregateCapitalized,
                "org.springframework.context.annotation.Bean"
            );
            boolean beanAdded = configAppender.appendBeanIfAbsent(mixin.getOutputDir(), configPackage,
                "infrastructure/bean-method-model", beanReplacements, imports, beanName);
            if (beanAdded) {
                System.out.println("Updated: DomainConfig.java with @Bean for " + aggregateCapitalized);
            }

            System.out.println("\nAggregate generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating aggregate: " + e.getMessage());
            return 1;
        }
    }
}
