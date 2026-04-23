package core;

import model.FileData;

public class PathScorer {

    public static int score(FileData file) {
        int score = 0;

        score += scorePathLength(file.getFilepath());
        score += scoreExtension(file.getFilename());
        score += scoreImportantDirectory(file.getFilepath());
        score += scoreRecency(file.getLastModified());

        return score;
    }

    // Shorter paths = more "root-level" = more important
    private static int scorePathLength(String path) {
        int depth = path.split("/").length;
        return Math.max(0, 10 - depth); // e.g. depth 3 → 7pts, depth 12 → 0pts
    }

    // Prioritize useful text formats over logs/configs
    private static int scoreExtension(String filename) {
        String ext = getExtension(filename);
        return switch (ext) {
            case "txt", "md", "java", "py" -> 5;
            case "json", "xml"             -> 3;
            case "log", "cfg", "conf"      -> 1;
            default                        -> 2;
        };
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot + 1).toLowerCase();
    }

    // Boost files sitting in "important" directories
    private static int scoreImportantDirectory(String path) {
        String lower = path.toLowerCase();
        if (lower.contains("/utcn/"))       return 5;
        if (lower.contains("/documents/"))  return 5;
        if (lower.contains("/src/"))        return 4;
        if (lower.contains("/desktop/"))    return 3;
        if (lower.contains("/downloads/"))  return 2;
        return 0;
    }

    // Recently modified files are more relevant
    private static int scoreRecency(long lastModified) {
        long ageInDays = (System.currentTimeMillis() - lastModified) / (1000 * 60 * 60 * 24);
        if (ageInDays < 7)   return 5;
        if (ageInDays < 30)  return 3;
        if (ageInDays < 365) return 1;
        return 0;
    }
}