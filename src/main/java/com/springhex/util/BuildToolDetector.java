package com.springhex.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BuildToolDetector {

    public enum BuildTool { MAVEN, GRADLE }

    public BuildTool detect(String baseDir) {
        Path dir = Paths.get(baseDir);
        if (Files.exists(dir.resolve("gradlew")) || Files.exists(dir.resolve("build.gradle")) || Files.exists(dir.resolve("build.gradle.kts"))) {
            return BuildTool.GRADLE;
        }
        if (Files.exists(dir.resolve("mvnw")) || Files.exists(dir.resolve("pom.xml"))) {
            return BuildTool.MAVEN;
        }
        return null;
    }

    public String resolveExecutable(String baseDir, BuildTool tool) {
        Path dir = Paths.get(baseDir);
        if (tool == BuildTool.MAVEN) {
            return Files.exists(dir.resolve("mvnw")) ? "./mvnw" : "mvn";
        }
        return Files.exists(dir.resolve("gradlew")) ? "./gradlew" : "gradle";
    }
}
