package core;

public class PreviewGenerator {
    public static String generatePreview(String content) {
        if (content == null || content.isEmpty()) return "No content available.";

        String[] lines = content.split("\n");
        StringBuilder preview = new StringBuilder();

        // Show the first 3 lines
        int linesToInclude = Math.min(3, lines.length);
        for (int i = 0; i < linesToInclude; i++) {
            preview.append(lines[i].trim()).append("\n");
        }

        String result = preview.toString().trim();
        boolean hasMoreLines = lines.length > 3;

        // Show max 100-characters
        if (result.length() > 100) {
            return result.substring(0, 100) + "...";
        }

        // If it's under 100 chars but had more than 3 lines originally
        if (hasMoreLines) {
            return result + "\n...";
        }

        return result;
    }
}