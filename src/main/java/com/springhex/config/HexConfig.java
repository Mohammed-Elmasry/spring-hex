package com.springhex.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HexConfig {

    private final String basePackage;
    private final Map<String, String> paths;
    private final Map<String, String> crud;
    private final boolean present;

    private HexConfig(String basePackage, Map<String, String> paths, Map<String, String> crud, boolean present) {
        this.basePackage = basePackage;
        this.paths = paths;
        this.crud = crud;
        this.present = present;
    }

    public static HexConfig load(String outputDir) {
        Path configPath = Paths.get(outputDir, ".hex", "config.yml");

        try (InputStream in = Files.newInputStream(configPath)) {
            Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
            @SuppressWarnings("unchecked")
            Map<String, Object> root = yaml.load(in);
            if (root == null) {
                return empty();
            }

            Object basePkgValue = root.get("base-package");
            String basePackage = null;
            if (basePkgValue instanceof String s) {
                basePackage = s;
            } else if (basePkgValue != null) {
                System.err.println("Warning: 'base-package' in .hex/config.yml must be a string, got: " + basePkgValue.getClass().getSimpleName());
            }

            Map<String, String> paths = toStringMap(root.get("paths"), "paths");
            Map<String, String> crud = toStringMap(root.get("crud"), "crud");

            return new HexConfig(basePackage, paths, crud, true);
        } catch (NoSuchFileException e) {
            return empty();
        } catch (IOException | ClassCastException e) {
            System.err.println("Error: Could not parse .hex/config.yml: " + e.getMessage());
            return empty();
        }
    }

    private static HexConfig empty() {
        return new HexConfig(null, Collections.emptyMap(), Collections.emptyMap(), false);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> toStringMap(Object obj, String sectionName) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        if (!(obj instanceof Map)) {
            System.err.println("Warning: '" + sectionName + "' in .hex/config.yml must be a map, got: " + obj.getClass().getSimpleName());
            return Collections.emptyMap();
        }
        Map<String, Object> raw = (Map<String, Object>) obj;
        Map<String, String> result = new HashMap<>();
        for (var entry : raw.entrySet()) {
            if (entry.getValue() instanceof String s) {
                result.put(entry.getKey(), s);
            } else if (entry.getValue() != null) {
                System.err.println("Warning: '" + sectionName + "." + entry.getKey() + "' must be a string, got: " + entry.getValue().getClass().getSimpleName());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    public String getBasePackage() {
        return basePackage;
    }

    public Map<String, String> getPaths() {
        return paths;
    }

    public Map<String, String> getCrud() {
        return crud;
    }

    public boolean isPresent() {
        return present;
    }
}
