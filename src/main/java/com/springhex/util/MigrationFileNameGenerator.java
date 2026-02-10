package com.springhex.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MigrationFileNameGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public String generateFlywayFileName(String name) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String normalized = normalizeName(name);
        return "V" + timestamp + "__" + normalized + ".sql";
    }

    public String generateFlywayRevertFileName(String originalName) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String normalized = normalizeName(originalName);
        return "V" + timestamp + "__revert_" + normalized + ".sql";
    }

    public String generateLiquibaseFileName(String name, String format) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String normalized = normalizeName(name);
        return timestamp + "_" + normalized + "." + format;
    }

    private String normalizeName(String name) {
        return name.toLowerCase().replace(' ', '_').replace('-', '_');
    }
}
