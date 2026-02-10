package com.springhex.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileGenerator {

    public void generate(Path outputPath, String content) throws IOException {
        // Create parent directories if they don't exist
        Path parentDir = outputPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Check if file already exists
        if (Files.exists(outputPath)) {
            throw new IOException("File already exists: " + outputPath + 
                ". Use --force to overwrite (not implemented yet).");
        }

        // Write the file
        Files.writeString(outputPath, content, StandardCharsets.UTF_8);
    }
}
