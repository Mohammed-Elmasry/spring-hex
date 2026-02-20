package com.springhex.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConfigAppender {

    private static final String SOURCE_DIR = "src/main/java";

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;

    public ConfigAppender() {
        this.stubProcessor = new StubProcessor();
        this.fileGenerator = new FileGenerator();
    }

    public Path resolveConfigPath(String outputDir, String configPackage) {
        String packagePath = configPackage.replace('.', '/');
        return Paths.get(outputDir, SOURCE_DIR, packagePath, "DomainConfig.java");
    }

    /**
     * Returns true if DomainConfig.java exists in the target project.
     */
    public boolean configExists(String outputDir, String configPackage) {
        return Files.exists(resolveConfigPath(outputDir, configPackage));
    }

    public void ensureConfigExists(String outputDir, String configPackage) throws IOException {
        Path configPath = resolveConfigPath(outputDir, configPackage);
        if (!Files.exists(configPath)) {
            var replacements = java.util.Map.of(
                    "{{BASE_PACKAGE}}", configPackage,
                    "{{PACKAGE}}", configPackage
            );
            String content = stubProcessor.process("infrastructure/domain-config", replacements);
            fileGenerator.generate(configPath, content);
            System.out.println("Created: " + configPath);
        }
    }

    public void appendBean(String outputDir, String configPackage,
                           String beanStubName,
                           java.util.Map<String, String> replacements,
                           List<String> newImports) throws IOException {
        Path configPath = resolveConfigPath(outputDir, configPackage);
        String content = Files.readString(configPath, StandardCharsets.UTF_8);

        // Add missing imports
        for (String imp : newImports) {
            String importLine = "import " + imp + ";";
            if (!content.contains(importLine)) {
                int lastImport = content.lastIndexOf("import ");
                int endOfLastImport = content.indexOf(';', lastImport) + 1;
                content = content.substring(0, endOfLastImport) + "\n" + importLine + content.substring(endOfLastImport);
            }
        }

        // Load bean snippet and apply replacements
        String snippet = stubProcessor.process(beanStubName, replacements);

        // Insert before the last closing brace
        int lastBrace = content.lastIndexOf('}');
        content = content.substring(0, lastBrace) + snippet + "\n" + content.substring(lastBrace);

        Files.writeString(configPath, content, StandardCharsets.UTF_8);
    }

    /**
     * Appends a @Bean method to DomainConfig only if:
     * 1. DomainConfig.java exists (if absent, silently skips — do not auto-create)
     * 2. A bean method with the given beanMethodName does not already exist in the file
     *
     * This is the safe variant used by make:model, make:entity, and make:aggregate to
     * avoid duplicate registrations on repeated command invocations.
     *
     * Import insertion is handled robustly: when no import statements exist yet the new
     * import is placed after the package declaration line rather than after a non-existent
     * import block.
     *
     * @return true if a bean was actually inserted, false if skipped (absent file or duplicate)
     */
    public boolean appendBeanIfAbsent(String outputDir, String configPackage,
                                      String beanStubName,
                                      java.util.Map<String, String> replacements,
                                      List<String> newImports,
                                      String beanMethodName) throws IOException {
        if (!configExists(outputDir, configPackage)) {
            return false;
        }

        Path configPath = resolveConfigPath(outputDir, configPackage);
        String content = Files.readString(configPath, StandardCharsets.UTF_8);

        // Duplicate guard: skip if a method with this exact name already exists
        if (content.contains(" " + beanMethodName + "(")) {
            return false;
        }

        // Add missing imports with robust handling for files that have no imports yet
        for (String imp : newImports) {
            String importLine = "import " + imp + ";";
            if (!content.contains(importLine)) {
                content = insertImport(content, importLine);
            }
        }

        // Load bean snippet and apply replacements
        String snippet = stubProcessor.process(beanStubName, replacements);

        // Insert before the last closing brace
        int lastBrace = content.lastIndexOf('}');
        if (lastBrace == -1) {
            throw new IOException("DomainConfig.java appears malformed — no closing brace found: " + configPath);
        }
        content = content.substring(0, lastBrace) + snippet + "\n" + content.substring(lastBrace);

        Files.writeString(configPath, content, StandardCharsets.UTF_8);
        return true;
    }

    /**
     * Inserts an import statement into file content in the correct position:
     * - After the last existing import statement, if any imports exist
     * - After the package declaration line, if no imports exist yet
     *
     * If neither a package declaration nor any imports are found the import is
     * prepended at the top, which is a safe fallback for unusual file structures.
     */
    private String insertImport(String content, String importLine) {
        int lastImportIdx = content.lastIndexOf("import ");
        if (lastImportIdx != -1) {
            // There are existing imports — insert after the last one
            int endOfLastImport = content.indexOf(';', lastImportIdx) + 1;
            return content.substring(0, endOfLastImport) + "\n" + importLine + content.substring(endOfLastImport);
        }

        // No existing imports — insert after the package declaration line
        int packageIdx = content.indexOf("package ");
        if (packageIdx != -1) {
            int endOfPackage = content.indexOf(';', packageIdx) + 1;
            return content.substring(0, endOfPackage) + "\n\n" + importLine + content.substring(endOfPackage);
        }

        // Fallback: prepend
        return importLine + "\n" + content;
    }
}
