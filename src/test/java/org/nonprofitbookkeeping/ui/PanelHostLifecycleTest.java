package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

class PanelHostLifecycleTest
{
    @Test
    void saveOnDismissWhenSwitchingAcrossReportsFundsInventory()
    {
        Map<AppPanelId, TestPanel> panels = new EnumMap<>(AppPanelId.class);
        panels.put(AppPanelId.REPORTS_WORKSPACE, new TestPanel(true));
        panels.put(AppPanelId.FUNDS, new TestPanel(false));
        panels.put(AppPanelId.INVENTORY, new TestPanel(false));

        PanelHost host = new PanelHost(id -> panels.get(id));
        host.show(AppPanelId.REPORTS_WORKSPACE);
        assertTrue(host.isActiveDirty());

        host.show(AppPanelId.FUNDS);
        host.show(AppPanelId.INVENTORY);

        assertEquals(1, panels.get(AppPanelId.REPORTS_WORKSPACE).saveCount);
        assertEquals(1, panels.get(AppPanelId.FUNDS).saveCount);
    }

    private static class TestPanel implements AppPanel, PanelHost.DirtyAwarePanel
    {
        private final boolean dirty;
        private int saveCount;

        TestPanel(boolean dirty)
        {
            this.dirty = dirty;
        }

        public String title()
        {
            return "test";
        }

        public Node root()
        {
            return new Pane();
        }

        public void onSave()
        {
            saveCount++;
        }

        public boolean isDirty()
        {
            return dirty;
        }
    }
}
