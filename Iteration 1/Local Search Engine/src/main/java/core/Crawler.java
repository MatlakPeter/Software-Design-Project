package core;

import model.FileData;
import repository.FileRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

public class Crawler {
    private String ignoreExtension;
    private FileRepository repository;
    private Set<String> scannedPaths;

    private int filesScannedCount; // counter for feedback while crawling

    private int added = 0;
    private int updated = 0;
    private int ignored = 0;

    private static final int _1MB = 1024 * 1024;
    private static final int LARGE_FILE_SAVE_LENGTH = _1MB / 2;

    private static final Set<String> TEXT_FILE_EXTENSIONS = Set.of(
            "txt", "md", "csv", "log", "json", "xml", "html", "htm",
            "yaml", "yml", "ini", "cfg", "conf",
            "java", "c", "cpp", "h", "hpp", "py", "js", "ts", "css",
            "sh", "bat", "sql", "properties", "gradle"
    );

    public Crawler(String ignoreExtension, FileRepository repository) {
        this.ignoreExtension = ignoreExtension;
        this.repository = repository;
        this.scannedPaths = new HashSet<>();
    }

    public Set<String> getScannedPaths() {
        return scannedPaths;
    }
    public void resetScannedPaths() {
        scannedPaths.clear();
        filesScannedCount = 0;
    }

    public List<FileData> scanDirectory(File directory) {
        List<FileData> results = new ArrayList<>();
        scanDirectoryRecursive(directory, results);
        return results;
    }

    public void scanDirectoryRecursive(File directory, List<FileData> results) {
        File[] files = directory.listFiles();
        if (files == null) { // Error handling for permissions
            System.out.println("Warning: Access denied or not a directory -> " + directory.getAbsolutePath());
            return;
        }

        for (File file : files) {
            filesScannedCount++;
            if (filesScannedCount % 1000 == 0) { // Print feeckack for every 1000th file
                System.out.println("... Still scanning. Files checked: " + filesScannedCount + " (Currently at: " + file.getParent() + ")");
            }

            if (Files.isSymbolicLink(file.toPath())) { return; }

            if (file.isDirectory()) {
                scanDirectoryRecursive(file, results);
            } else if (file.isFile() && isTextFile(file) && !file.getName().endsWith(ignoreExtension)) {
                results.add(buildFileData(file));
            }
        }
    }

    private boolean isTextFile(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1 || lastDot == name.length() - 1) { // no extension
            return false;
        }
        String ext = name.substring(lastDot + 1).toLowerCase();
        return TEXT_FILE_EXTENSIONS.contains(ext);
    }

    private FileData buildFileData(File file) {
        try {
            scannedPaths.add(file.getAbsolutePath());
            String content = "";

            try {
                // Attempt 1: Standard UTF-8
                content = Files.readString(Path.of(file.getAbsolutePath()), StandardCharsets.UTF_8);
            } catch (MalformedInputException | UnmappableCharacterException e) {
                try {
                    // Attempt 2: Fallback to Central European Windows encoding
                    content = Files.readString(Path.of(file.getAbsolutePath()), Charset.forName("windows-1250"));
                } catch (MalformedInputException | UnmappableCharacterException e2) {
                    // Attempt 3: Ultimate fallback. ISO-8859-1 reads almost anything without throwing errors.
                    content = Files.readString(Path.of(file.getAbsolutePath()), StandardCharsets.ISO_8859_1);
                }
            }

            // sanitize the string before saving it to the database, because PostgreSQL cannot store the null byte (0x00).
            if (content != null) {
                content = content.replace("\u0000", "");

                if (content.length() > _1MB) {
                    content = content.substring(0, LARGE_FILE_SAVE_LENGTH);
                }
            }

            FileData fileData = new FileData(
                    file.getName(),
                    file.getAbsolutePath(),
                    content,
                    file.lastModified()
            );
            return fileData;

        } catch (IOException e) {
             System.err.println("Could not read file: " + file.getAbsolutePath() + " | Reason: " + e.getClass().getSimpleName());
        }
        return null;
    }
}