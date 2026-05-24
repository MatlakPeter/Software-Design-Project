package core.processor;

import model.FileData;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImageFileProcessor implements FileProcessor {

    private static final int SAMPLE_STRIDE = 50; // how many pixels to skip between samples

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp"
    );

    @Override
    public boolean supports(File file) {
        String ext = getExtension(file.getName());
        return SUPPORTED_EXTENSIONS.contains(ext);
    }

    @Override
    public FileData process(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                System.err.println("Could not decode image: " + file.getAbsolutePath());
                return null;
            }

            String dominantColor = extractDominantColor(image);

            FileData fileData = new FileData(
                    file.getName(),
                    file.getAbsolutePath(),
                    "", // no text content for images
                    file.lastModified(),
                    dominantColor
            );
            return fileData;

        } catch (IOException e) {
            System.err.println("Could not read image: " + file.getAbsolutePath()
                    + " | Reason: " + e.getClass().getSimpleName());
            return null;
        }
    }

    private String extractDominantColor(BufferedImage image) {
        Map<String, Integer> colorCounts = new HashMap<>();

        int width  = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y += SAMPLE_STRIDE) {
            for (int x = 0; x < width; x += SAMPLE_STRIDE) {
                int rgb = image.getRGB(x, y);
                String colorName = toColorName(rgb);
                colorCounts.merge(colorName, 1, Integer::sum);
            }
        }

        return colorCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("unknown");
    }

    private String toColorName(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8)  & 0xFF;
        int b =  rgb        & 0xFF;

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        float hue        = hsb[0] * 360f; // 0–360
        float saturation = hsb[1];        // 0–1
        float brightness = hsb[2];        // 0–1

        // Achromatic checks come first (saturation or brightness too low to have a hue)
        if (brightness < 0.15f) return "black";
        if (brightness > 0.90f && saturation < 0.10f) return "white";
        if (saturation < 0.15f) return "gray";

        // Chromatic hue buckets
        if (hue < 15f  || hue >= 345f) return "red";
        if (hue < 45f)                 return "orange";
        if (hue < 75f)                 return "yellow";
        if (hue < 150f)                return "green";
        if (hue < 195f)                return "cyan";
        if (hue < 255f)                return "blue";
        if (hue < 285f)                return "purple";
        return "pink"; // 285–345
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot == -1 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }
}
