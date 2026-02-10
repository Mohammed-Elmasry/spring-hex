package com.springhex.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LiquibaseChangelogUpdater {

    public void addIncludeToXmlChangelog(Path masterFile, String changesetPath) throws IOException {
        String content = Files.readString(masterFile, StandardCharsets.UTF_8);
        String includeTag = "    <include file=\"" + changesetPath + "\"/>";

        if (content.contains(changesetPath)) {
            return;
        }

        int insertPos = content.lastIndexOf("</databaseChangeLog>");
        if (insertPos == -1) {
            throw new IOException("Invalid master changelog: missing </databaseChangeLog> tag in " + masterFile);
        }

        String updated = content.substring(0, insertPos) + includeTag + "\n" + content.substring(insertPos);
        Files.writeString(masterFile, updated, StandardCharsets.UTF_8);
    }

    public void addIncludeToYamlChangelog(Path masterFile, String changesetPath) throws IOException {
        String content = Files.readString(masterFile, StandardCharsets.UTF_8);

        if (content.contains(changesetPath)) {
            return;
        }

        String includeEntry = "\n- include:\n    file: " + changesetPath;
        String updated = content + includeEntry + "\n";
        Files.writeString(masterFile, updated, StandardCharsets.UTF_8);
    }
}
