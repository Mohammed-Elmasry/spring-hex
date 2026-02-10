package com.springhex.command;

import com.springhex.config.ConfigResolver;
import com.springhex.config.ResolvedConfig;
import com.springhex.config.ConfigurationException;
import com.springhex.generator.FileGenerator;
import com.springhex.generator.StubProcessor;
import com.springhex.util.PackageResolver;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "make:test",
    mixinStandardHelpOptions = true,
    description = "Generate a test class (feature test by default, --unit for unit test)"
)
public class MakeTestCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Test subject name (e.g., OrderController, OrderService)")
    private String name;

    @Option(names = "--unit", description = "Generate a unit test instead of a feature test")
    private boolean unit;

    @Mixin
    private GeneratorMixin mixin;

    private final StubProcessor stubProcessor;
    private final FileGenerator fileGenerator;
    private final PackageResolver packageResolver;

    public MakeTestCommand() {
        this.stubProcessor = new StubProcessor();
        this.fileGenerator = new FileGenerator();
        this.packageResolver = new PackageResolver();
    }

    @Override
    public Integer call() {
        try {
            ResolvedConfig config = ConfigResolver.resolve(mixin.getOutputDir(), mixin.getBasePackage());
            String resolvedPackage = config.getBasePackage();

            String testName = name.endsWith("Test") ? name : name + "Test";
            String subPackage = unit ? "unit" : "feature";
            String testPackage = resolvedPackage + "." + subPackage;
            String stubName = unit ? "test/unit-test" : "test/feature-test";

            Map<String, String> replacements = new HashMap<>();
            replacements.put("{{TEST_PACKAGE}}", testPackage);
            replacements.put("{{TEST_NAME}}", testName);

            String content = stubProcessor.process(stubName, replacements);
            Path outputPath = packageResolver.resolveTestOutputPath(mixin.getOutputDir(), testName, testPackage);
            fileGenerator.generate(outputPath, content);
            System.out.println("Created: " + outputPath);

            String testType = unit ? "Unit" : "Feature";
            System.out.println("\n" + testType + " test generated successfully!");
            return 0;
        } catch (ConfigurationException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error generating test: " + e.getMessage());
            return 1;
        }
    }
}
