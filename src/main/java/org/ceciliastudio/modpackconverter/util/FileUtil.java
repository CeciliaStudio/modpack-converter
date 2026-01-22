package org.ceciliastudio.modpackconverter.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtil {
    /**
     * 拷贝文件或目录到目标目录内。
     *
     * @param path 要拷贝的文件或目录路径。
     * @param destination 目标目录，path 的内容会作为子项放在该目录下。
     * @throws IOException 发生 I/O 错误时抛出。
     */
    public static void copy(Path path, Path destination) throws IOException {
        if (Files.isDirectory(path)) {
            Path targetDir = destination.resolve(path.getFileName());
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path rel = path.relativize(dir);
                    Path target = targetDir.resolve(rel);
                    if (Files.notExists(target)) {
                        Files.createDirectories(target);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path rel = path.relativize(file);
                    Path target = targetDir.resolve(rel);
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            if (Files.notExists(destination)) {
                Files.createDirectories(destination);
            }
            Path target = destination.resolve(path.getFileName());
            Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
