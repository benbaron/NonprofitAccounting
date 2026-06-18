package nonprofitbookkeeping.alternate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AlternatePanelScaffoldSourceTest
{
    @Test
    void scaffoldDefinesCommonRegionsAndStateMethods() throws IOException
    {
        String source = Files.readString(Path.of("src/main/java/org/nonprofitbookkeeping/ui/AlternatePanelScaffold.java"));

        assertTrue(source.contains("setPrimaryActions"));
        assertTrue(source.contains("setSecondaryActions"));
        assertTrue(source.contains("setFilterBar"));
        assertTrue(source.contains("setContent"));
        assertTrue(source.contains("setStatus"));
        assertTrue(source.contains("setWarningBanner"));
        assertTrue(source.contains("showEmpty"));
        assertTrue(source.contains("showLoading"));
        assertTrue(source.contains("showError"));
    }

    @Test
    void chartAndLedgerUseSharedAlternatePanelScaffold() throws IOException
    {
        String chart = Files.readString(Path.of("src/main/java/org/nonprofitbookkeeping/ui/ChartOfAccountsPanel.java"));
        String ledger = Files.readString(Path.of("src/main/java/org/nonprofitbookkeeping/ui/LedgerRegisterPanel.java"));

        assertTrue(chart.contains("new AlternatePanelScaffold(\"Chart of Accounts\")"));
        assertTrue(ledger.contains("new AlternatePanelScaffold(\"Ledger Register\")"));
    }

    @Test
    void themeResourcesDeclareScaffoldStyleClasses() throws IOException
    {
        for (String file : new String[] { "ui-system.css", "light.css", "dark.css" })
        {
            String css = Files.readString(Path.of("src/main/resources/themes", file));
            assertTrue(css.contains(".alternate-panel-scaffold"), file);
            assertTrue(css.contains(".alternate-panel-warning-banner") || css.contains(".alternate-panel-banner"), file);
            assertTrue(css.contains(".alternate-panel-empty-state") || css.contains(".alternate-panel-state"), file);
        }
    }
}
