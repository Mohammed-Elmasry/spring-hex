package com.springhex.config;

import com.springhex.util.PackageDetector;

import java.nio.file.Path;
import java.util.Optional;

public final class ConfigResolver {

    private ConfigResolver() {}

    public static ResolvedConfig resolve(String outputDir, String explicitPackage) {
        HexConfig hexConfig = HexConfig.load(outputDir);

        String basePackage = resolveBasePackage(explicitPackage, hexConfig, outputDir);
        if (basePackage == null) {
            throw new ConfigurationException("Could not detect base package. Please specify with -p option or create .hex/config.yml using 'spring-hex init'.");
        }

        HexPathResolver pathResolver = new HexPathResolver(basePackage, hexConfig);
        return new ResolvedConfig(basePackage, hexConfig, pathResolver);
    }

    private static String resolveBasePackage(String explicitPackage, HexConfig hexConfig, String outputDir) {
        if (explicitPackage != null && !explicitPackage.isBlank()) {
            return explicitPackage;
        }

        String configPkg = hexConfig.getBasePackage();
        if (configPkg != null && !configPkg.isBlank()) {
            System.out.println("Using base package from .hex/config.yml: " + configPkg);
            return configPkg;
        }

        PackageDetector detector = new PackageDetector();
        Optional<String> detected = detector.detect(Path.of(outputDir));
        if (detected.isPresent()) {
            System.out.println("Auto-detected base package: " + detected.get());
            return detected.get();
        }

        return null;
    }
}
