package core.processor;

import model.FileData;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class TextFileProcessor implements FileProcessor {
    private static final int _1MB = 1024 * 1024;
    private static final int LARGE_FILE_SAVE_LENGTH = _1MB / 2;

    private static final Set<String> TEXT_FILE_EXTENSIONS = Set.of(
            "txt", "md", "csv", "log", "json", "xml", "html", "htm",
            "yaml", "yml", "ini", "cfg", "conf",
            "java", "c", "cpp", "h", "hpp", "py", "js", "ts", "css",
            "sh", "bat", "sql", "properties", "gradle"
    );

    @Override
    public boolean supports(File file) {
        String ext = getExtension(file.getName());
        return TEXT_FILE_EXTENSIONS.contains(ext);
    }

    @Override
    public FileData process(File file) {
        try {
            String content = readWithFallback(file);

            // sanitize the string before saving it to the database, because PostgreSQL cannot store the null byte (0x00).
            if (content != null) {
                content = content.replace("\u0000", "");
                if (content.length() > _1MB) {
                    content = content.substring(0, LARGE_FILE_SAVE_LENGTH);
                }
            }

            return new FileData(
                    file.getName(),
                    file.getAbsolutePath(),
                    content,
                    file.lastModified()
            );

        } catch (IOException e) {
            System.err.println("Could not read file: " + file.getAbsolutePath()
                    + " | Reason: " + e.getClass().getSimpleName());
            return null;
        }
    }

    private String readWithFallback(File file) throws IOException {
        try {
            return Files.readString(Path.of(file.getAbsolutePath()), StandardCharsets.UTF_8);
        } catch (MalformedInputException | UnmappableCharacterException e) {
            try {
                // Fallback to Central European Windows encoding
                return Files.readString(Path.of(file.getAbsolutePath()), Charset.forName("windows-1250"));
            } catch (MalformedInputException | UnmappableCharacterException e2) {
                // Ultimate fallback — ISO-8859-1 reads almost anything without throwing.
                return Files.readString(Path.of(file.getAbsolutePath()), StandardCharsets.ISO_8859_1);
            }
        }
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot == -1 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }
}
