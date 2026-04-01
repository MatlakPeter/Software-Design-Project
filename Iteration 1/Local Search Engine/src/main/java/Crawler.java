import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Crawler {
    private String ignoreExtension;
    private FileRepository repository;
    private Set<String> scannedPaths;

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
    }

    public void scanDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files == null) { // Error handling for permissions
            System.out.println("Warning: Access denied or not a directory -> " + directory.getAbsolutePath());
            return;
        }

        for (File file : files) {
            if (Files.isSymbolicLink(file.toPath())) {
                continue; // Prevent infinite loops
            }

            if (file.isDirectory()) {
                scanDirectory(file);
            } else if (file.isFile() && isTextFile(file) && !file.getName().endsWith(ignoreExtension)) {
                processFile(file);
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

    private void processFile(File file) {
        try {
            scannedPaths.add(file.getAbsolutePath());

            String content = Files.readString(Path.of(file.getAbsolutePath()));
            FileData fileData = new FileData(
                    file.getName(),
                    file.getAbsolutePath(),
                    content,
                    file.lastModified()
            );
            repository.saveOrUpdateFile(fileData);
        } catch (IOException e) {
            System.err.println("Could not read file: " + file.getAbsolutePath());
        }
    }
}