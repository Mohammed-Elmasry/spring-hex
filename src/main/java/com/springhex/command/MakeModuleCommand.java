package com.springhex.command;

import com.springhex.generator.ConfigAppender;
import com.springhex.config.HexPathResolver;
import com.springhex.config.ConfigResolver;
import com.springhex.config.ResolvedConfig;
import com.springhex.config.ConfigurationException;
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
    name = "make:module",
    mixinStandardHelpOptions = true,
    description = "Generate a full bounded context module (aggregate, CQRS, persistence, controller)"
)
public class MakeModuleCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Module/aggregate name (e.g., Order)")
    private String moduleName;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;
    private final ConfigAppender configAppender;

    public MakeModuleCommand() {
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

            String aggregateCapitalized = StringUtils.capitalize(moduleName);
            String aggregateLower = moduleName.toLowerCase();
            String aggregatePlural = StringUtils.pluralize(aggregateLower);
            int fileCount = 0;

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{AGGREGATE}}", aggregateLower);
            replacements.put("{{AGGREGATE_CAPITALIZED}}", aggregateCapitalized);
            replacements.put("{{AGGREGATE_PLURAL}}", aggregatePlural);
            replacements.put("{{ENTITY_NAME}}", aggregateCapitalized);
            replacements.put("{{TABLE_NAME}}", aggregatePlural);
            pathResolver.populatePackagePlaceholders(aggregateLower, replacements);

            // 1. Aggregate root
            String modelPackage = pathResolver.resolve("model", aggregateLower);
            generateFile("domain/aggregate", aggregateCapitalized, modelPackage, replacements);
            fileCount++;

            // 2. Aggregate ID value object
            Map<String, String> idReplacements = new HashMap<>(replacements);
            idReplacements.put("{{VALUE_OBJECT_NAME}}", aggregateCapitalized + "Id");
            generateFile("domain/value-object-id", aggregateCapitalized + "Id", modelPackage, idReplacements);
            fileCount++;

            // 3. Create command
            String commandPackage = pathResolver.resolve("command", aggregateLower);
            String createCommandName = "Create" + aggregateCapitalized + "Command";
            Map<String, String> cmdReplacements = new HashMap<>(replacements);
            cmdReplacements.put("{{COMMAND_NAME}}", createCommandName);
            generateFile("domain/command", createCommandName, commandPackage, cmdReplacements);
            fileCount++;

            // 4. Create command handler
            String createHandlerName = createCommandName + "Handler";
            generateFile("domain/command-handler", createHandlerName, commandPackage, cmdReplacements);
            fileCount++;

            // 5. Get query
            String queryPackage = pathResolver.resolve("query", aggregateLower);
            String getQueryName = "Get" + aggregateCapitalized + "Query";
            Map<String, String> queryReplacements = new HashMap<>(replacements);
            queryReplacements.put("{{QUERY_NAME}}", getQueryName);
            queryReplacements.put("{{RETURN_TYPE}}", "Object");
            generateFile("domain/query", getQueryName, queryPackage, queryReplacements);
            fileCount++;

            // 6. Get query handler
            String getHandlerName = getQueryName + "Handler";
            generateFile("domain/query-handler", getHandlerName, queryPackage, queryReplacements);
            fileCount++;

            // 7. Repository port
            String portPackage = pathResolver.resolve("port-out", aggregateLower);
            generateFile("domain/repository-port", aggregateCapitalized + "Repository", portPackage, replacements);
            fileCount++;

            // 8. Input port (use case)
            String inputPortPackage = pathResolver.resolve("port-in", aggregateLower);
            Map<String, String> inputPortReplacements = new HashMap<>(replacements);
            inputPortReplacements.put("{{PORT_NAME}}", aggregateCapitalized + "UseCase");
            generateFile("domain/input-port", aggregateCapitalized + "UseCase", inputPortPackage, inputPortReplacements);
            fileCount++;

            // 9. Request DTO
            String dtoPackage = pathResolver.resolve("dto", aggregateLower);
            Map<String, String> requestReplacements = new HashMap<>(replacements);
            requestReplacements.put("{{REQUEST_NAME}}", "Create" + aggregateCapitalized + "Request");
            generateFile("domain/request", "Create" + aggregateCapitalized + "Request", dtoPackage, requestReplacements);
            fileCount++;

            // 10. Response DTO
            Map<String, String> responseReplacements = new HashMap<>(replacements);
            responseReplacements.put("{{RESPONSE_NAME}}", aggregateCapitalized + "Response");
            generateFile("domain/response", aggregateCapitalized + "Response", dtoPackage, responseReplacements);
            fileCount++;

            // 11. JPA Entity
            String persistencePackage = pathResolver.resolve("persistence", aggregateLower);
            generateFile("infrastructure/jpa-entity", aggregateCapitalized + "JpaEntity", persistencePackage, replacements);
            fileCount++;

            // 12. Spring Data Repository
            generateFile("infrastructure/spring-data-repository", aggregateCapitalized + "JpaRepository", persistencePackage, replacements);
            fileCount++;

            // 13. Repository Adapter
            generateFile("infrastructure/repository-adapter", aggregateCapitalized + "RepositoryAdapter", persistencePackage, replacements);
            fileCount++;

            // 14. Mapper
            generateFile("infrastructure/mapper", aggregateCapitalized + "Mapper", persistencePackage, replacements);
            fileCount++;

            // 15. Controller
            String controllerPackage = pathResolver.resolve("controller", aggregateLower);
            generateFile("infrastructure/controller", aggregateCapitalized + "Controller", controllerPackage, replacements);
            fileCount++;

            // Ensure DomainConfig exists and append @Bean methods for handlers
            String configPackage = pathResolver.resolveStatic("config");
            configAppender.ensureConfigExists(mixin.getOutputDir(), configPackage);

            // Bean for Create command handler
            String createHandlerBean = Character.toLowerCase(createHandlerName.charAt(0)) + createHandlerName.substring(1);
            Map<String, String> createBeanReplacements = new HashMap<>();
            createBeanReplacements.put("{{HANDLER_CLASS}}", createHandlerName);
            createBeanReplacements.put("{{HANDLER_BEAN}}", createHandlerBean);
            createBeanReplacements.put("{{AGGREGATE_CAPITALIZED}}", aggregateCapitalized);

            List<String> createImports = List.of(
                pathResolver.resolve("command", aggregateLower) + "." + createHandlerName,
                pathResolver.resolve("port-out", aggregateLower) + "." + aggregateCapitalized + "Repository",
                "org.springframework.context.annotation.Bean"
            );

            configAppender.appendBean(mixin.getOutputDir(), configPackage,
                    "infrastructure/bean-method-handler", createBeanReplacements, createImports);
            System.out.println("Updated: DomainConfig.java with @Bean for " + createHandlerName);

            // Bean for Get query handler
            String getHandlerBean = Character.toLowerCase(getHandlerName.charAt(0)) + getHandlerName.substring(1);
            Map<String, String> getBeanReplacements = new HashMap<>();
            getBeanReplacements.put("{{HANDLER_CLASS}}", getHandlerName);
            getBeanReplacements.put("{{HANDLER_BEAN}}", getHandlerBean);
            getBeanReplacements.put("{{AGGREGATE_CAPITALIZED}}", aggregateCapitalized);

            List<String> getImports = List.of(
                pathResolver.resolve("query", aggregateLower) + "." + getHandlerName
            );

            configAppender.appendBean(mixin.getOutputDir(), configPackage,
                    "infrastructure/bean-method-handler", getBeanReplacements, getImports);
            System.out.println("Updated: DomainConfig.java with @Bean for " + getHandlerName);

            System.out.println("\nModule generated successfully!");
            System.out.println("Generated " + fileCount + " files + DomainConfig update for " + aggregateCapitalized);
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating module: " + e.getMessage());
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
