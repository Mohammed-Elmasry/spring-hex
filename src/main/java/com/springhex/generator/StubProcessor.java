package com.springhex.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class StubProcessor {

    private static final String STUBS_PATH = "stubs/";
    private static final String STUB_EXTENSION = ".stub";

    public String process(String stubName, Map<String, String> replacements) throws IOException {
        String template = loadStub(stubName);
        return applyReplacements(template, replacements);
    }

    public String loadStub(String stubName) throws IOException {
        String resourcePath = STUBS_PATH + stubName + STUB_EXTENSION;
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Stub template not found: " + resourcePath);
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    public String applyReplacements(String template, Map<String, String> replacements) {
        String result = template;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
