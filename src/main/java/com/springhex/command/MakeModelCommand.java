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
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:model",
    mixinStandardHelpOptions = true,
    description = "Generate a domain model class"
)
public class MakeModelCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Model name (e.g., Order, Product)")
    private String modelName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;
    private final ConfigAppender configAppender;

    public MakeModelCommand() {
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

            String model = StringUtils.capitalize(modelName);
            String aggregateLower = aggregate.toLowerCase();

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{ENTITY_NAME}}", model);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // Generate Domain Model
            String domainPackage = pathResolver.resolve("model", aggregateLower);
            generateFile("domain/model", model, domainPackage, replacements);

            // Register @Bean in DomainConfig if it exists
            String configPackage = pathResolver.resolveStatic("config");
            String beanName = Character.toLowerCase(model.charAt(0)) + model.substring(1);
            Map<String, String> beanReplacements = new HashMap<>();
            beanReplacements.put("{{CLASS_NAME}}", model);
            beanReplacements.put("{{BEAN_NAME}}", beanName);
            List<String> imports = List.of(
                domainPackage + "." + model,
                "org.springframework.context.annotation.Bean"
            );
            boolean beanAdded = configAppender.appendBeanIfAbsent(mixin.getOutputDir(), configPackage,
                "infrastructure/bean-method-model", beanReplacements, imports, beanName);
            if (beanAdded) {
                System.out.println("Updated: DomainConfig.java with @Bean for " + model);
            }

            System.out.println("\nDomain model generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating model: " + e.getMessage());
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
