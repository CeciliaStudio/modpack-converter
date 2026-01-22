package org.ceciliastudio.modpackconverter;

import org.ceciliastudio.modpackconverter.util.FileUtil;
import org.ceciliastudio.modpackconverter.util.I18n;
import org.ceciliastudio.modpackconverter.util.Logger;
import org.ceciliastudio.modpackconverter.util.ZipUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class Converter {
    @SuppressWarnings("SpellCheckingInspection")
    private static final List<String> IGNORED_FILES = List.of(
            // Launchers
            "PCL", "hmclversion.cfg", ".PCL_Mac.json",
            // Generated at runtime
            "logs", "crash-reports", "screenshots", "backups", "command_history.txt", "usercache.json", ".fabric", "saves"
    );

    private static Optional<Path> findInstanceRoot(Path modpackRoot) throws IOException {
        if (!Files.isDirectory(modpackRoot)) return Optional.empty();
        try (Stream<Path> stream = Files.walk(modpackRoot, 3)) {
            return stream.filter(path -> {
                if (!Files.isDirectory(path)) return false;
                String fileName = path.getFileName().toString();
                return Files.exists(path.resolve(fileName + ".json"))
                        && Files.exists(path.resolve(fileName + ".jar"));
            }).findFirst();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void generateModrinthManifest(String name, String versionId, String summary, Map<String, String> dependencies, Path destination) throws IOException {
        List<String> dependencyStrings = new ArrayList<>();
        dependencies.forEach((key, value) -> dependencyStrings.add("\"%s\": \"%s\"".formatted(key, value)));
        String content = "{ \"formatVersion\": 1, \"game\": \"minecraft\", \"name\": \"%s\", \"versionId\": \"%s\", \"summary\": \"%s\", \"files\": [], \"dependencies\": { %s } }".formatted(name, versionId, summary, String.join(", ", dependencyStrings));
        Files.writeString(destination, content);
    }

    private static void copyInstanceFiles(Path source, Path destination) throws IOException {
        try (Stream<Path> stream = Files.list(source)) {
            stream.forEach(path -> {
                if (IGNORED_FILES.contains(path.getFileName().toString()) || path.getFileName().toString().startsWith("natives")) return;
                try {
                    FileUtil.copy(path, destination);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    public static void convert(Path modpackPath, Path destination) throws IOException {
        Path tempDirectory = Files.createTempDirectory("modpackconverter");
        Path mrpackDirectory = Files.createDirectory(tempDirectory.resolve("mrpack"));
        String name = modpackPath.getFileName().toString().replaceFirst("\\.[^.]+$", "");
        try {
            Logger.info("log.extracting_modpack");
            ZipUtil.unzip(modpackPath, tempDirectory);
            Logger.info("log.searching_instance");
            Optional<Path> instanceRoot = findInstanceRoot(tempDirectory);
            if (instanceRoot.isEmpty()) {
                Logger.error("log.no_instances_was_found");
                throw new RuntimeException(I18n.localized("log.no_instances_was_found"));
            }
            Logger.info("log.generating_mrpack");
            generateModrinthManifest(name, "未知", "未知", Map.of("minecraft", "1.21"), mrpackDirectory.resolve("modrinth.index.json"));
            Logger.info("log.copying_files");
            copyInstanceFiles(instanceRoot.get(), mrpackDirectory.resolve("overrides"));
            Logger.info("log.creating_archive");
            ZipUtil.zip(mrpackDirectory, destination);
            Logger.info("log.modpack_convert_finished");
        } finally {
            Files.walkFileTree(tempDirectory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private Converter() {
    }
}
