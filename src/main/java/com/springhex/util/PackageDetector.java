package com.springhex.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageDetector {

    public Optional<String> detect() {
        return detect(Paths.get("."));
    }

    public Optional<String> detect(Path projectRoot) {
        // First try: Look for @SpringBootApplication class
        Optional<String> fromSpringBoot = detectFromSpringBootMain(projectRoot);
        if (fromSpringBoot.isPresent()) {
            return fromSpringBoot;
        }

        // Second try: Extract from pom.xml groupId
        Optional<String> fromPom = detectFromPom(projectRoot);
        if (fromPom.isPresent()) {
            return fromPom;
        }

        // Third try: Scan src/main/java for first package
        return detectFromSources(projectRoot);
    }

    private Optional<String> detectFromSpringBootMain(Path projectRoot) {
        Path srcMainJava = projectRoot.resolve("src/main/java");
        if (!Files.exists(srcMainJava)) {
            return Optional.empty();
        }

        try {
            return Files.walk(srcMainJava)
                .filter(p -> p.toString().endsWith(".java"))
                .filter(this::isSpringBootMainClass)
                .findFirst()
                .flatMap(this::extractPackage);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private boolean isSpringBootMainClass(Path javaFile) {
        try {
            String content = Files.readString(javaFile);
            return content.contains("@SpringBootApplication");
        } catch (IOException e) {
            return false;
        }
    }

    private Optional<String> extractPackage(Path javaFile) {
        try {
            String content = Files.readString(javaFile);
            Pattern pattern = Pattern.compile("^package\\s+([\\w.]+);", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        } catch (IOException e) {
            // ignore
        }
        return Optional.empty();
    }

    private Optional<String> detectFromPom(Path projectRoot) {
        Path pomPath = projectRoot.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return Optional.empty();
        }

        try {
            String content = Files.readString(pomPath);
            
            // Try to get groupId (skip parent's groupId)
            // Simple approach: find groupId that's not inside <parent> block
            String withoutParent = content.replaceAll("(?s)<parent>.*?</parent>", "");
            
            Pattern pattern = Pattern.compile("<groupId>([^<]+)</groupId>");
            Matcher matcher = pattern.matcher(withoutParent);
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        } catch (IOException e) {
            // ignore
        }
        return Optional.empty();
    }

    private Optional<String> detectFromSources(Path projectRoot) {
        Path srcMainJava = projectRoot.resolve("src/main/java");
        if (!Files.exists(srcMainJava)) {
            return Optional.empty();
        }

        try {
            // Find first directory structure under src/main/java
            return Files.walk(srcMainJava, 4)
                .filter(Files::isDirectory)
                .filter(p -> !p.equals(srcMainJava))
                .map(p -> srcMainJava.relativize(p).toString())
                .filter(p -> !p.isEmpty())
                .map(p -> p.replace('/', '.').replace('\\', '.'))
                .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
