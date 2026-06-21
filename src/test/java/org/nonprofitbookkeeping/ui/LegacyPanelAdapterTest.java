package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javafx.scene.layout.VBox;

class LegacyPanelAdapterTest
{
    @Test
    void saveContextDelegatesToLegacyOnSave()
    {
        TrackingPanel panel = new TrackingPanel();
        LegacyPanelAdapter.AdaptedPanel adapted = LegacyPanelAdapter.from(panel);

        adapted.saveContext();

        assertEquals(1, panel.saveCalls);
    }

    private static final class TrackingPanel implements AppPanel
    {
        private int saveCalls;

        @Override
        public String title()
        {
            return "Tracking";
        }

        @Override
        public javafx.scene.Node root()
        {
            return new VBox();
        }

        @Override
        public void onSave()
        {
            this.saveCalls++;
        }
    }
}
