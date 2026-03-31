public class PreviewGenerator {
    public static String generatePreview(String content) {
        if (content == null || content.isEmpty()) return "No content available.";

        String[] lines = content.split("\n");
        StringBuilder preview = new StringBuilder();

        // Return up to the first 3 lines
        int linesToInclude = Math.min(3, lines.length);
        for (int i = 0; i < linesToInclude; i++) {
            preview.append(lines[i].trim()).append("\n");
        }

        return preview.toString().trim() + (lines.length > 3 ? "\n..." : "");
    }
}