package com.springhex.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MigrationToolDetector {

    public enum MigrationTool { FLYWAY, LIQUIBASE }

    public MigrationTool detect(String baseDir) {
        Path dir = Paths.get(baseDir);

        // 1. Check existing directories
        if (Files.isDirectory(dir.resolve("src/main/resources/db/migration"))) {
            return MigrationTool.FLYWAY;
        }
        if (Files.isDirectory(dir.resolve("src/main/resources/db/changelog"))) {
            return MigrationTool.LIQUIBASE;
        }

        // 2. Check application.properties
        Path appProps = dir.resolve("src/main/resources/application.properties");
        if (Files.exists(appProps)) {
            try {
                String content = Files.readString(appProps);
                if (content.contains("spring.flyway.")) return MigrationTool.FLYWAY;
                if (content.contains("spring.liquibase.")) return MigrationTool.LIQUIBASE;
            } catch (IOException ignored) {}
        }

        // 3. Check application.yml / application.yaml
        for (String ymlName : new String[]{"application.yml", "application.yaml"}) {
            Path ymlPath = dir.resolve("src/main/resources/" + ymlName);
            if (Files.exists(ymlPath)) {
                try {
                    String content = Files.readString(ymlPath);
                    if (content.contains("flyway:")) return MigrationTool.FLYWAY;
                    if (content.contains("liquibase:")) return MigrationTool.LIQUIBASE;
                } catch (IOException ignored) {}
            }
        }

        // 4. Check pom.xml
        Path pomPath = dir.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            try {
                String content = Files.readString(pomPath);
                if (content.contains("flyway-core")) return MigrationTool.FLYWAY;
                if (content.contains("liquibase-core")) return MigrationTool.LIQUIBASE;
            } catch (IOException ignored) {}
        }

        // 5. Check build.gradle / build.gradle.kts
        for (String gradleName : new String[]{"build.gradle", "build.gradle.kts"}) {
            Path gradlePath = dir.resolve(gradleName);
            if (Files.exists(gradlePath)) {
                try {
                    String content = Files.readString(gradlePath);
                    if (content.contains("flyway")) return MigrationTool.FLYWAY;
                    if (content.contains("liquibase")) return MigrationTool.LIQUIBASE;
                } catch (IOException ignored) {}
            }
        }

        return null;
    }

    public boolean shouldWarnFlywayOutOfOrder(String baseDir) {
        Path appProps = Paths.get(baseDir, "src/main/resources/application.properties");
        if (!Files.exists(appProps)) {
            return true;
        }
        try {
            String content = Files.readString(appProps);
            return !content.contains("spring.flyway.out-of-order=true");
        } catch (IOException e) {
            return true;
        }
    }
}
