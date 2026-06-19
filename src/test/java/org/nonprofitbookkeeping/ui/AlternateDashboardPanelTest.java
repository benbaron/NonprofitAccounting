package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.bridge.dashboard.DashboardDataBridge;
import org.nonprofitbookkeeping.service.FundBalanceRow;

class AlternateDashboardPanelTest
{
    @Test
    void noDatabaseDashboardModelRendersEmptyAndNotWiredStatesWithoutDemoValues()
    {
        UiSessionContext context = new UiSessionContext();
        List<AlternateDashboardModel.Card> cards = new AlternateDashboardModel().cards(context, null,
            "Open a database to load service-backed accounting metrics.", null, 0, List.of());

        String text = render(cards);

        assertTrue(text.contains("Active database status"));
        assertTrue(text.contains("Not open"));
        assertTrue(text.contains(AlternateDashboardPanel.NOT_WIRED_STATE));
        assertTrue(text.contains("Open a database to load service-backed accounting metrics."));
        assertFalse(text.contains("$11,230"));
        assertFalse(text.contains("$5,830"));
        assertFalse(text.contains("$23,009"));
        assertFalse(text.contains("Payee A"));
        assertFalse(text.contains("Program Supplies"));
    }

    @Test
    void serviceBackedFundBalancesRenderWhenAvailable()
    {
        UiSessionContext context = new UiSessionContext();
        context.openDatabase(Path.of("/tmp/demo.mv.db"));
        DashboardDataBridge.DashboardSnapshot snapshot = new DashboardDataBridge.DashboardSnapshot(
            List.of(new FundBalanceRow("GEN", "General", new BigDecimal("12.34"))), 2, 1);

        String text = render(new AlternateDashboardModel().cards(context, snapshot, null, 0, 0, List.of()));

        assertTrue(text.contains("1 funds"));
        assertTrue(text.contains("GEN General"));
        assertTrue(text.contains("2 posting accounts"));
        assertTrue(text.contains("1 active funds"));
    }

    private static String render(List<AlternateDashboardModel.Card> cards)
    {
        return cards.stream()
            .map(card -> card.title() + "\n" + card.value() + "\n" + card.detail())
            .collect(Collectors.joining("\n"));
    }
}
