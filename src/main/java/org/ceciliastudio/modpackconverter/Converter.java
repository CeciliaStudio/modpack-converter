package org.ceciliastudio.modpackconverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Converter {
    public static Path findInstanceRoot(Path modpackRoot) throws IOException {
        if (!Files.isDirectory(modpackRoot)) return null;
        try (Stream<Path> stream = Files.walk(modpackRoot, 3)) {
            return stream.filter(path -> {
                if (!Files.isDirectory(path)) return false;
                String fileName = path.getFileName().toString();
                return Files.exists(path.resolve(fileName + ".json"))
                        && Files.exists(path.resolve(fileName + ".jar"));
            }).findFirst().orElse(null);
        }
    }

    private Converter() {
    }
}
