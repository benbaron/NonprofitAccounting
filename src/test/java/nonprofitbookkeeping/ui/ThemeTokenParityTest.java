package nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ThemeTokenParityTest
{
    private static final Pattern TOKEN = Pattern.compile("\\s*(-npbk-[a-z0-9-]+)\\s*:");

    @Test
    void lightAndDarkDefineSameNpbkRootTokens() throws IOException
    {
        Set<String> light = tokens(Path.of("src/main/resources/themes/light.css"));
        Set<String> dark = tokens(Path.of("src/main/resources/themes/dark.css"));
        assertEquals(light, dark,
            () -> "Theme token mismatch. light-only=" + diff(light, dark)
                + ", dark-only=" + diff(dark, light));
    }

    private static Set<String> tokens(Path file) throws IOException
    {
        Set<String> tokens = new LinkedHashSet<>();
        for (String line : Files.readAllLines(file))
        {
            Matcher m = TOKEN.matcher(line);
            if (m.find())
            {
                tokens.add(m.group(1));
            }
        }
        return tokens;
    }

    private static Set<String> diff(Set<String> a, Set<String> b)
    {
        Set<String> d = new LinkedHashSet<>(a);
        d.removeAll(b);
        return d;
    }
}
