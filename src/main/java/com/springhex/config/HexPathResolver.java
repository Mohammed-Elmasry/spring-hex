package com.springhex.config;

import java.util.HashMap;
import java.util.Map;

public class HexPathResolver {

    private static final Map<String, String> HEX_DEFAULTS = Map.ofEntries(
            Map.entry("model", "domain.{aggregate}.model"),
            Map.entry("command", "domain.{aggregate}.command"),
            Map.entry("query", "domain.{aggregate}.query"),
            Map.entry("event", "domain.{aggregate}.event"),
            Map.entry("event-listener", "infrastructure.event.{aggregate}"),
            Map.entry("dto", "domain.{aggregate}.dto"),
            Map.entry("port-in", "domain.{aggregate}.port.in"),
            Map.entry("port-out", "domain.{aggregate}.port.out"),
            Map.entry("persistence", "infrastructure.persistence.{aggregate}"),
            Map.entry("controller", "infrastructure.web.{aggregate}"),
            Map.entry("adapter", "infrastructure.{category}.{aggregate}"),
            Map.entry("config", "infrastructure.config"),
            Map.entry("mediator", "infrastructure.mediator"),
            Map.entry("cqrs", "domain.cqrs"),
            Map.entry("domain-root", "domain"),
            Map.entry("service", "domain.{aggregate}.service")
    );

    private static final Map<String, String> CRUD_DEFAULTS = Map.of(
            "model", "{name}.model",
            "entity", "{name}.entity",
            "repository", "{name}.repository",
            "mapper", "{name}.mapper",
            "service", "{name}.service",
            "controller", "{name}.web"
    );

    private final String basePackage;
    private final HexConfig config;

    public HexPathResolver(String basePackage, HexConfig config) {
        this.basePackage = basePackage;
        this.config = config;
    }

    public String resolve(String key, String aggregate) {
        String pattern = getHexPattern(key);
        String resolved = pattern.replace("{aggregate}", aggregate);
        return basePackage + "." + resolved;
    }

    public String resolve(String key, Map<String, String> vars) {
        String pattern = getHexPattern(key);
        String resolved = pattern;
        for (var entry : vars.entrySet()) {
            resolved = resolved.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return basePackage + "." + resolved;
    }

    public String resolveStatic(String key) {
        String pattern = getHexPattern(key);
        return basePackage + "." + pattern;
    }

    public String resolveCrud(String key, String name) {
        String pattern = getCrudPattern(key);
        String resolved = pattern.replace("{name}", name);
        return basePackage + "." + resolved;
    }

    public void populatePackagePlaceholders(String aggregate, Map<String, String> replacements) {
        replacements.put("{{PACKAGE_MODEL}}", resolve("model", aggregate));
        replacements.put("{{PACKAGE_PORT_OUT}}", resolve("port-out", aggregate));
        replacements.put("{{PACKAGE_PORT_IN}}", resolve("port-in", aggregate));
        replacements.put("{{PACKAGE_CQRS}}", resolveStatic("cqrs"));
        replacements.put("{{PACKAGE_MEDIATOR}}", resolveStatic("mediator"));
        replacements.put("{{PACKAGE_EVENT}}", resolve("event", aggregate));
        replacements.put("{{PACKAGE_DOMAIN_ROOT}}", resolveStatic("domain-root"));
    }

    public void populateCrudPackagePlaceholders(String name, Map<String, String> replacements) {
        replacements.put("{{PACKAGE_CRUD_MODEL}}", resolveCrud("model", name));
        replacements.put("{{PACKAGE_CRUD_ENTITY}}", resolveCrud("entity", name));
        replacements.put("{{PACKAGE_CRUD_REPOSITORY}}", resolveCrud("repository", name));
        replacements.put("{{PACKAGE_CRUD_SERVICE}}", resolveCrud("service", name));
        replacements.put("{{PACKAGE_CRUD_MAPPER}}", resolveCrud("mapper", name));
    }

    private String getHexPattern(String key) {
        Map<String, String> configPaths = config.getPaths();
        if (configPaths.containsKey(key)) {
            return configPaths.get(key);
        }
        return HEX_DEFAULTS.getOrDefault(key, key);
    }

    private String getCrudPattern(String key) {
        Map<String, String> configCrud = config.getCrud();
        if (configCrud.containsKey(key)) {
            return configCrud.get(key);
        }
        return CRUD_DEFAULTS.getOrDefault(key, key);
    }
}
