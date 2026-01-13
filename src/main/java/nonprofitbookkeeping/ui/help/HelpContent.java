package nonprofitbookkeeping.ui.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

/**
 * Utility methods for loading help documentation resources.
 * Provides a shared fallback text so callers can degrade gracefully
 * when the rich HTML content cannot be displayed.
 */
public final class HelpContent
{
        private static final Logger LOGGER = LoggerFactory.getLogger(HelpContent.class);

        private static final String FALLBACK_TEXT = String.join("\n",
                "Nonprofit Bookkeeping",
                "",
                "Keyboard shortcuts:",
                "  • Ctrl+S — Save current record",
                "  • Ctrl+O — Open company file",
                "  • F1 — Open this help window",
                "",
                "Full documentation is available in the docs/ folder shipped with the application.");

        private HelpContent()
        {
                // Utility class
        }

        /**
         * Attempts to load a help document from the classpath.
         *
         * @param resourcePath the absolute resource path (e.g. "/help/index.html").
         * @return an {@link Optional} containing the loaded text if successful; otherwise empty.
         */
        public static Optional<String> loadHelpDocument(String resourcePath)
        {
                try (InputStream in = HelpContent.class.getResourceAsStream(resourcePath))
                {
                        if (in == null)
                        {
                                return Optional.empty();
                        }

                        String contents = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining("\n"));
                        return Optional.of(contents);
                }
                catch (IOException ex)
                {
                        LOGGER.debug("Unable to load help document from {}", resourcePath, ex);
                        return Optional.empty();
                }
        }

        /**
         * Provides a plain-text fallback description of the help content.
         *
         * @return fallback help text.
         */
        public static String fallbackText()
        {
                return FALLBACK_TEXT;
        }
}
