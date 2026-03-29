package org.nonprofitbookkeeping.ui;

import javafx.scene.control.ListView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportLibraryPanelReachabilityTest
{
    @BeforeAll
    static void setupFx()
    {
        FxTestSupport.initToolkitOrSkip();
    }

    @Test
    void coreReportsRemainReachableAndRetiredEntriesRemoved()
    {
        FxTestSupport.onFx(() -> {
            ReportLibraryPanel panel = new ReportLibraryPanel();
            List<String> entries = reportEntries(panel);

            assertTrue(entries.contains("Income Statement"));
            assertTrue(entries.contains("Balance Sheet"));
            assertTrue(entries.contains("Account Details"));

            assertFalse(entries.contains("Trial Balance"));
            assertFalse(entries.contains("General Ledger Detail"));
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static List<String> reportEntries(ReportLibraryPanel panel) throws Exception
    {
        Field reportListField = ReportLibraryPanel.class.getDeclaredField("reportList");
        reportListField.setAccessible(true);
        ListView<String> reportList = (ListView<String>) reportListField.get(panel);
        return List.copyOf(reportList.getItems());
    }
}
