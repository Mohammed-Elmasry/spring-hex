package com.springhex.command;

import com.springhex.config.HexPathResolver;
import com.springhex.config.ConfigResolver;
import com.springhex.config.ResolvedConfig;
import com.springhex.config.ConfigurationException;
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
    name = "make:query",
    mixinStandardHelpOptions = true,
    description = "Generate a CQRS query and its handler"
)
public class MakeQueryCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Name of the query (e.g., GetOrderById)")
    private String queryName;

    @Option(names = {"-a", "--aggregate"}, description = "Aggregate name (e.g., order)", required = true)
    private String aggregate;

    @Option(names = {"--no-handler"}, description = "Skip generating the query handler", defaultValue = "false")
    private boolean noHandler;

    @Option(names = {"-r", "--return-type"}, description = "Return type for the query handler", defaultValue = "Object")
    private String returnType;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;
    private final ConfigAppender configAppender;

    public MakeQueryCommand() {
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

            String className = normalizeQueryName(queryName);
            String aggregateLower = aggregate.toLowerCase();
            String aggregateCapitalized = StringUtils.capitalize(aggregate);

            String queryPackage = pathResolver.resolve("query", aggregateLower);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{AGGREGATE_CAPITALIZED}}", aggregateCapitalized);
            replacements.put("{{QUERY_NAME}}", className);
            replacements.put("{{RETURN_TYPE}}", returnType);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // Generate Query class
            String queryContent = stubProcessor.process("domain/query", replacements);
            Path queryPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), className, queryPackage);
            fileGenerator.generate(queryPath, queryContent);
            System.out.println("Created: " + queryPath);

            // Generate QueryHandler class if requested
            if (!noHandler) {
                String handlerClassName = className + "Handler";
                String handlerContent = stubProcessor.process("domain/query-handler", replacements);
                Path handlerPath = packageResolver.resolveOutputPath(mixin.getOutputDir(), handlerClassName, queryPackage);
                fileGenerator.generate(handlerPath, handlerContent);
                System.out.println("Created: " + handlerPath);

                // Ensure DomainConfig exists and append @Bean method
                String configPackage = pathResolver.resolveStatic("config");
                configAppender.ensureConfigExists(mixin.getOutputDir(), configPackage);

                String beanName = Character.toLowerCase(handlerClassName.charAt(0)) + handlerClassName.substring(1);
                Map<String, String> beanReplacements = new HashMap<>();
                beanReplacements.put("{{HANDLER_CLASS}}", handlerClassName);
                beanReplacements.put("{{HANDLER_BEAN}}", beanName);
                beanReplacements.put("{{AGGREGATE_CAPITALIZED}}", aggregateCapitalized);

                List<String> imports = List.of(
                    pathResolver.resolve("query", aggregateLower) + "." + handlerClassName,
                    pathResolver.resolve("port-out", aggregateLower) + "." + aggregateCapitalized + "Repository",
                    "org.springframework.context.annotation.Bean"
                );

                configAppender.appendBean(mixin.getOutputDir(), configPackage,
                        "infrastructure/bean-method-handler", beanReplacements, imports);
                System.out.println("Updated: DomainConfig.java with @Bean for " + handlerClassName);
            }

            System.out.println("\nQuery generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating query: " + e.getMessage());
            return 1;
        }
    }

    private String normalizeQueryName(String name) {
        String capitalized = StringUtils.capitalize(name);
        if (!capitalized.endsWith("Query")) {
            return capitalized + "Query";
        }
        return capitalized;
    }
}
