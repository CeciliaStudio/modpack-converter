package org.ceciliastudio.modpackconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    /**
     * 压缩指定目录下的所有文件和子目录（不包含该目录本身）。
     *
     * @param contentRoot 要压缩的目录，仅包含其内部的内容，根目录自身不会包含在归档中。
     * @param destination 生成的压缩包路径。
     * @throws IOException 发生 I/O 错误时抛出。
     */
    public static void zip(Path contentRoot, Path destination) throws IOException {
        try (OutputStream fos = Files.newOutputStream(destination);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            Files.walkFileTree(contentRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Path relativePath = contentRoot.relativize(file);
                    zos.putNextEntry(new ZipEntry(relativePath.toString().replace("\\", "/")));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                    if (!contentRoot.equals(dir)) {
                        Path relativePath = contentRoot.relativize(dir).resolve("");
                        zos.putNextEntry(new ZipEntry(relativePath.toString().replace("\\", "/") + "/"));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    /**
     * 解压 ZIP 压缩包到指定目录，会自动创建目标目录（如果不存在）。
     * 解压内容为压缩包内所有文件和子目录，保持原有目录结构。
     *
     * @param archivePath ZIP 压缩包路径。
     * @param destination 目标解压目录，不存在时会自动创建。
     * @throws IOException 发生 I/O 错误时抛出。
     */
    public static void unzip(Path archivePath, Path destination) throws IOException {
        if (Files.notExists(destination)) {
            Files.createDirectories(destination);
        }
        try (ZipFile zipFile = new ZipFile(archivePath.toFile())) {
            zipFile.stream().forEach(entry -> {
                try {
                    Path outPath = destination.resolve(entry.getName()).normalize();
                    if (!outPath.startsWith(destination)) {
                        throw new IOException("Entry is outside of the target dir: " + entry.getName());
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(outPath);
                    } else {
                        Files.createDirectories(outPath.getParent());
                        try (InputStream in = zipFile.getInputStream(entry)) {
                            Files.copy(in, outPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private ZipUtil() {
    }
}
