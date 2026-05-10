package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

class LegacyPanelAdapterTest
{
    @Test
    void saveContextDoesNotInvokeLegacyOnSave()
    {
        class StubPanel implements AppPanel
        {
            int saveCalls;

            public String title()
            {
                return "Stub";
            }

            public Node root()
            {
                return new VBox();
            }

            public void onSave()
            {
                saveCalls++;
            }
        }

        StubPanel panel = new StubPanel();
        LegacyPanelAdapter.AdaptedPanel adapted = LegacyPanelAdapter.from(panel);

        adapted.saveContext();

        assertEquals(0, panel.saveCalls,
            "Alternate-shell context save should remain in-memory and must not commit via onSave().");
    }
}
