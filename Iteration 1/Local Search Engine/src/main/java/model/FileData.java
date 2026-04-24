package model;

public class FileData {
    private String filename;
    private String filepath;
    private String content;
    private long lastModified;
    private int path_score;

    public FileData(String filename, String filepath, String content, long lastModified) {
        this.filename = filename;
        this.filepath = filepath;
        this.content = content;
        this.lastModified = lastModified;
        this.path_score = 0;
    }
    public FileData(String filename, String filepath, String content, long lastModified, int path_score) {
        this.filename = filename;
        this.filepath = filepath;
        this.content = content;
        this.lastModified = lastModified;
        this.path_score = path_score;
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

    public int getPathScore() {
        return path_score;
    }
    public void setPathScore(int path_score) {
        this.path_score = path_score;
    }
}