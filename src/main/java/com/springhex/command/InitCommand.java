package com.springhex.command;

import com.springhex.generator.StubProcessor;
import com.springhex.util.PackageDetector;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(
    name = "init",
    mixinStandardHelpOptions = true,
    description = "Initialize .hex/config.yml in the current project"
)
public class InitCommand implements Callable<Integer> {

    @Option(names = {"-p", "--package"}, description = "Base package (auto-detected if not specified)")
    private String basePackage;

    @Option(names = {"-o", "--output"}, description = "Output directory (defaults to current directory)", defaultValue = ".")
    private String outputDir;

    @Option(names = "--force", description = "Overwrite existing config", defaultValue = "false")
    private boolean force;

    private final PackageDetector packageDetector;
    private final StubProcessor stubProcessor;

    public InitCommand() {
        this.packageDetector = new PackageDetector();
        this.stubProcessor = new StubProcessor();
    }

    @Override
    public Integer call() {
        Path configPath = Paths.get(outputDir, ".hex", "config.yml");

        if (Files.exists(configPath) && !force) {
            System.err.println("Error: .hex/config.yml already exists. Use --force to overwrite.");
            return 1;
        }

        String resolvedPackage = resolvePackage();
        if (resolvedPackage == null) {
            System.err.println("Error: Could not detect base package. Please specify with -p option.");
            return 1;
        }

        try {
            Path hexDir = configPath.getParent();
            if (!Files.exists(hexDir)) {
                Files.createDirectories(hexDir);
            }

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{BASE_PACKAGE}}", resolvedPackage);

            String content = stubProcessor.process("init/config", replacements);
            Files.writeString(configPath, content, StandardCharsets.UTF_8);

            System.out.println("Created: " + configPath);
            System.out.println();
            System.out.println("Configuration:");
            System.out.println("  base-package: " + resolvedPackage);
            System.out.println();
            System.out.println("Next steps:");
            System.out.println("  spring-hex make:module <AggregateName>   Generate a full bounded context");
            System.out.println("  spring-hex make:crud <EntityName>        Generate a CRUD resource");
            System.out.println("  spring-hex --help                        See all available commands");
            return 0;
        } catch (IOException e) {
            System.err.println("Error creating .hex/config.yml: " + e.getMessage());
            return 1;
        }
    }

    private String resolvePackage() {
        if (basePackage != null && !basePackage.isBlank()) {
            return basePackage;
        }

        Optional<String> detected = packageDetector.detect(Path.of(outputDir));
        if (detected.isPresent()) {
            System.out.println("Auto-detected base package: " + detected.get());
            return detected.get();
        }

        return null;
    }
}
