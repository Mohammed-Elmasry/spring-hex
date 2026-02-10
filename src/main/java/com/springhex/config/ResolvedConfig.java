package com.springhex.config;

public class ResolvedConfig {

    private final String basePackage;
    private final HexConfig hexConfig;
    private final HexPathResolver pathResolver;

    public ResolvedConfig(String basePackage, HexConfig hexConfig, HexPathResolver pathResolver) {
        this.basePackage = basePackage;
        this.hexConfig = hexConfig;
        this.pathResolver = pathResolver;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public HexConfig getHexConfig() {
        return hexConfig;
    }

    public HexPathResolver getPathResolver() {
        return pathResolver;
    }
}
