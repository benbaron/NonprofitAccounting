package nonprofitbookkeeping.reports;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utility class for discovering Jasper report templates bundled with the application.
 */
public final class ReportTemplateScanner {
    private static final String JRXML_DIR = "jrxml";

    private ReportTemplateScanner() {}

    /**
     * Scans the {@code src/main/resources/jrxml} directory on the classpath and
     * returns a map of display names to report type keys.
     *
     * @return map where each key is a user friendly display name and the value
     *         is the corresponding report type key used by {@link ReportContext}.
     */
    public static Map<String, String> discoverTemplates() {
        Map<String, String> templates = new LinkedHashMap<>();
        ClassLoader cl = ReportTemplateScanner.class.getClassLoader();
        URL url = cl.getResource(JRXML_DIR);
        if (url == null) {
            return templates;
        }
        try {
            Path dir = Paths.get(url.toURI());
            try (Stream<Path> stream = Files.list(dir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".jrxml"))
                      .forEach(p -> {
                          String fileName = p.getFileName().toString();
                          String base = fileName.substring(0, fileName.length() - ".jrxml".length());
                          if (base.endsWith("Alt")) {
                              base = base.substring(0, base.length() - 3);
                          }
                          String display = toDisplayName(base);
                          String key = toKey(base);
                          templates.putIfAbsent(display, key);
                      });
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return templates;
    }

    private static String toDisplayName(String base) {
        String withSpaces = base.replace('_', ' ');
        withSpaces = withSpaces.replaceAll("([a-z])([A-Z])", "$1 $2");
        String[] parts = withSpaces.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1).toLowerCase())
              .append(' ');
        }
        return sb.toString().trim();
    }

    private static String toKey(String base) {
        String snake = base.replaceAll("([a-z])([A-Z])", "$1_$2")
                           .replace('-', '_')
                           .replace(' ', '_')
                           .toLowerCase();
        return snake + "_jasper";
    }
}
