package com.springhex.command;

import com.springhex.generator.FileGenerator;
import com.springhex.generator.StubProcessor;
import com.springhex.util.PackageResolver;
import com.springhex.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import com.springhex.config.HexPathResolver;
import com.springhex.config.ConfigResolver;
import com.springhex.config.ResolvedConfig;
import com.springhex.config.ConfigurationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:crud",
    mixinStandardHelpOptions = true,
    description = "Generate a simple MVC CRUD resource (model, entity, repository, service, mapper, controller)"
)
public class MakeCrudCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Entity name (e.g., User, Product)")
    private String entityName;

    @Option(names = {"--no-model"}, description = "Skip domain model generation", defaultValue = "false")
    private boolean noModel;

    @Option(names = {"--no-service"}, description = "Skip service layer generation", defaultValue = "false")
    private boolean noService;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeCrudCommand() {
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

            String capitalized = StringUtils.capitalize(entityName);
            String lower = entityName.toLowerCase();
            String plural = StringUtils.pluralize(lower);
            int fileCount = 0;

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);
            replacements.put("{{ENTITY_NAME}}", capitalized);
            replacements.put("{{ENTITY_NAME_LOWER}}", lower);
            replacements.put("{{ENTITY_NAME_PLURAL}}", plural);
            replacements.put("{{TABLE_NAME}}", plural);
            pathResolver.populateCrudPackagePlaceholders(lower, replacements);

            // 1. Domain model
            if (!noModel) {
                String modelPackage = pathResolver.resolveCrud("model", lower);
                generateFile("mvc/model", capitalized, modelPackage, replacements);
                fileCount++;
            }

            // 2. JPA Entity
            String entityPackage = pathResolver.resolveCrud("entity", lower);
            generateFile("mvc/entity", capitalized + "Entity", entityPackage, replacements);
            fileCount++;

            // 3. Repository
            String repoPackage = pathResolver.resolveCrud("repository", lower);
            generateFile("mvc/repository", capitalized + "Repository", repoPackage, replacements);
            fileCount++;

            // 4. Mapper
            String mapperPackage = pathResolver.resolveCrud("mapper", lower);
            generateFile("mvc/mapper", capitalized + "Mapper", mapperPackage, replacements);
            fileCount++;

            // 5. Service
            if (!noService) {
                String servicePackage = pathResolver.resolveCrud("service", lower);
                generateFile("mvc/service", capitalized + "Service", servicePackage, replacements);
                fileCount++;
            }

            // 6. Controller
            String controllerPackage = pathResolver.resolveCrud("controller", lower);
            generateFile("mvc/controller", capitalized + "Controller", controllerPackage, replacements);
            fileCount++;

            System.out.println("\nCRUD resource generated successfully!");
            System.out.println("Generated " + fileCount + " files for " + capitalized);
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating CRUD resource: " + e.getMessage());
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
