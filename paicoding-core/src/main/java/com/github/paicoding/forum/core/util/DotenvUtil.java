package com.github.paicoding.forum.core.util;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Loads .env-style files into JVM system properties before Spring starts.
 * Environment variables and explicit -D properties still take precedence.
 *
 * @author itwanger
 * @date 2026/3/23
 */
public final class DotenvUtil {
    private static final List<String> ENV_FILES = Arrays.asList(".env", ".env.local");
    private static volatile boolean loaded = false;

    private DotenvUtil() {
    }

    public static void load() {
        if (loaded) {
            return;
        }

        synchronized (DotenvUtil.class) {
            if (loaded) {
                return;
            }

            Set<String> protectedKeys = new HashSet<String>(System.getenv().keySet());
            protectedKeys.addAll(System.getProperties().stringPropertyNames());
            for (Path envFile : findEnvFiles()) {
                loadEnvFile(envFile, protectedKeys);
            }
            loaded = true;
        }
    }

    public static String get(String key) {
        load();
        String value = System.getenv(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return StringUtils.trimToNull(System.getProperty(key));
    }

    public static String getFirst(String... keys) {
        load();
        if (keys == null) {
            return null;
        }

        for (String key : keys) {
            String value = get(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    public static String require(String key) {
        return requireFirst(key);
    }

    public static String requireFirst(String... keys) {
        String value = getFirst(keys);
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException("Missing required config: " + String.join(", ", keys));
        }
        return value;
    }

    private static void loadEnvFile(Path path, Set<String> protectedKeys) {
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                EnvEntry entry = parseLine(rawLine);
                if (entry == null || protectedKeys.contains(entry.key)) {
                    continue;
                }
                System.setProperty(entry.key, entry.value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load env file: " + path, e);
        }
    }

    private static List<Path> findEnvFiles() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path repoRoot = findRepoRoot(cwd);
        List<Path> dirs = new ArrayList<Path>();
        if (cwd.startsWith(repoRoot)) {
            LinkedList<Path> chain = new LinkedList<Path>();
            Path cursor = cwd;
            while (cursor != null) {
                chain.addFirst(cursor);
                if (cursor.equals(repoRoot)) {
                    break;
                }
                cursor = cursor.getParent();
            }
            dirs.addAll(chain);
        } else {
            dirs.add(cwd);
        }

        List<Path> files = new ArrayList<Path>();
        for (Path dir : dirs) {
            for (String fileName : ENV_FILES) {
                Path candidate = dir.resolve(fileName);
                if (Files.isRegularFile(candidate)) {
                    files.add(candidate);
                }
            }
        }
        return files;
    }

    private static Path findRepoRoot(Path cwd) {
        Path cursor = cwd;
        Path repoRoot = cwd;
        while (cursor != null) {
            if (Files.exists(cursor.resolve(".git"))) {
                repoRoot = cursor;
            }
            cursor = cursor.getParent();
        }
        return repoRoot;
    }

    private static EnvEntry parseLine(String rawLine) {
        if (rawLine == null) {
            return null;
        }

        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return null;
        }

        if (line.startsWith("export ")) {
            line = line.substring("export ".length()).trim();
        }

        int separatorIndex = line.indexOf('=');
        if (separatorIndex <= 0) {
            return null;
        }

        String key = line.substring(0, separatorIndex).trim();
        if (key.isEmpty()) {
            return null;
        }

        String value = line.substring(separatorIndex + 1).trim();
        if (value.isEmpty()) {
            return new EnvEntry(key, "");
        }

        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        } else {
            int commentIndex = value.indexOf(" #");
            if (commentIndex >= 0) {
                value = value.substring(0, commentIndex).trim();
            }
        }

        return new EnvEntry(key, value);
    }

    private static class EnvEntry {
        private final String key;
        private final String value;

        private EnvEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
