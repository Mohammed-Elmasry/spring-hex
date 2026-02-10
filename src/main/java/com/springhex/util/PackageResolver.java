package com.springhex.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PackageResolver {

    private static final String DEFAULT_SOURCE_DIR = "src/main/java";
    private static final String TEST_SOURCE_DIR = "src/test/java";

    public Path resolveOutputPath(String className, String packageName) {
        return resolveOutputPath(".", className, packageName);
    }

    public Path resolveOutputPath(String baseDir, String className, String packageName) {
        String packagePath = packageName.replace('.', '/');
        String fileName = className + ".java";
        return Paths.get(baseDir, DEFAULT_SOURCE_DIR, packagePath, fileName);
    }

    public Path resolveTestOutputPath(String baseDir, String className, String packageName) {
        String packagePath = packageName.replace('.', '/');
        String fileName = className + ".java";
        return Paths.get(baseDir, TEST_SOURCE_DIR, packagePath, fileName);
    }

    public String packageToPath(String packageName) {
        return packageName.replace('.', '/');
    }

    public String pathToPackage(String path) {
        return path.replace('/', '.').replace('\\', '.');
    }

    public String extractPackageName(Path javaFile) {
        // Assumes standard Maven/Gradle structure: src/main/java/com/example/...
        String pathStr = javaFile.toString();
        int srcMainJavaIndex = pathStr.indexOf(DEFAULT_SOURCE_DIR);
        
        if (srcMainJavaIndex == -1) {
            return "";
        }
        
        String packagePath = pathStr.substring(srcMainJavaIndex + DEFAULT_SOURCE_DIR.length() + 1);
        // Remove the file name
        int lastSeparator = Math.max(packagePath.lastIndexOf('/'), packagePath.lastIndexOf('\\'));
        if (lastSeparator > 0) {
            packagePath = packagePath.substring(0, lastSeparator);
        }
        
        return pathToPackage(packagePath);
    }
}
