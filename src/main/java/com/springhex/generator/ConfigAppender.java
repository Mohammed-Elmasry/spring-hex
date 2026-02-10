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
}
