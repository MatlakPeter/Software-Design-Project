public class FileData {
    private String filename;
    private String filepath;
    private String content;
    private long lastModified;

    public FileData(String filename, String filepath, String content, long lastModified) {
        this.filename = filename;
        this.filepath = filepath;
        this.content = content;
        this.lastModified = lastModified;
    }

    // Getters
    public String getFilename() {
        return filename;
    }
    public String getFilepath() {
        return filepath;
    }
    public String getContent() {
        return content;
    }
    public long getLastModified() {
        return lastModified;
    }
}