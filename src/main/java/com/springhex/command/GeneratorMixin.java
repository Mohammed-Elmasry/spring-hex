package com.springhex.command;

import picocli.CommandLine.Option;

public class GeneratorMixin {

    @Option(names = {"-p", "--package"}, description = "Base package (auto-detected if not specified)")
    String basePackage;

    @Option(names = {"-o", "--output"}, description = "Output directory (defaults to current directory)", defaultValue = ".")
    String outputDir;

    public String getBasePackage() {
        return basePackage;
    }

    public String getOutputDir() {
        return outputDir;
    }
}
