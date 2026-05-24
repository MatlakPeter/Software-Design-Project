package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileData {
    private String filename;
    private String filepath;
    private String content;
    private long lastModified;
    private int path_score;
    private String dominantColor; // null for text files

    public FileData(String filename, String filepath, String content, long lastModified) {
        this.filename = filename;
        this.filepath = filepath;
        this.content = content;
        this.lastModified = lastModified;
        this.path_score = 0;
        this.dominantColor = null;
    }
    public FileData(String filename, String filepath, String content, long lastModified, String dominantColor) {
        this.filename = filename;
        this.filepath = filepath;
        this.content = content;
        this.lastModified = lastModified;
        this.path_score = 0;
        this.dominantColor = dominantColor;
    }
    public FileData(String filename, String filepath, String content, long lastModified, int path_score) {
        this.filename = filename;
        this.filepath = filepath;
        this.content = content;
        this.lastModified = lastModified;
        this.path_score = path_score;
        this.dominantColor = null;
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
    public String getDominantColor() {
        return dominantColor;
    }
    public String getFormattedDate() {
        Instant instant = Instant.ofEpochMilli(lastModified);
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
    public void setPathScore(int path_score) {
        this.path_score = path_score;
    }
    public void setDominantColor(String dominantColor) {
        this.dominantColor = dominantColor;
    }
}