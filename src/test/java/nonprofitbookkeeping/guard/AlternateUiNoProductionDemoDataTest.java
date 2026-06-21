package nonprofitbookkeeping.guard;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class AlternateUiNoProductionDemoDataTest
{
    private static final Path ALTERNATE_UI_ROOT = Path.of("src/main/java/org/nonprofitbookkeeping/ui");

    @Test
    void productionAlternateUiDoesNotContainKnownDemoAccountingRows() throws IOException
    {
        List<String> forbiddenSnippets = List.of(
            "Payee A",
            "Payee B",
            "Program Supplies",
            "Office Rent",
            "Volunteer Meals",
            "Laptop Fleet",
            "Office Furniture",
            "Using fallback demo accounts",
            "demoAccount(\"I.c\"",
            "$11,230",
            "$5,830",
            "$23,009",
            "$12,004",
            "$3,420",
            "$7,230",
            "$980");

        try (var files = Files.walk(ALTERNATE_UI_ROOT))
        {
            List<Path> alternateUiFiles = files
                .filter(path -> path.toString().endsWith(".java"))
                .toList();
            List<Path> offenders = alternateUiFiles.stream()
                .filter(path -> containsForbiddenSnippet(path, forbiddenSnippets))
                .toList();

            assertTrue(alternateUiFiles.size() > 0,
                () -> "Expected guard to inspect alternate UI Java files under " + ALTERNATE_UI_ROOT);
            assertTrue(offenders.isEmpty(),
                () -> "Production alternate UI still contains known demo accounting rows: " + offenders);
        }
    }

    private static boolean containsForbiddenSnippet(Path path, List<String> forbiddenSnippets)
    {
        try
        {
            String source = Files.readString(path);
            return forbiddenSnippets.stream().anyMatch(source::contains);
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Unable to read " + path, ex);
        }
    }
}
