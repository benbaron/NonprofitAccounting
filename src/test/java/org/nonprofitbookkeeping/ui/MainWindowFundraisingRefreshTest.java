package org.nonprofitbookkeeping.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.model.AppPreferencesState;
import org.nonprofitbookkeeping.model.DatabaseSelectionState;
import org.nonprofitbookkeeping.model.MultiCompanyState;
import org.nonprofitbookkeeping.model.UiThemePreference;
import org.nonprofitbookkeeping.model.UserPrivilegeLevel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies fundraising panel refresh behavior when session context changes.
 */
class MainWindowFundraisingRefreshTest
{
    @BeforeAll
    static void setupToolkit()
    {
        FxTestSupport.initToolkitOrSkip();
    }

    @Test
    void activeFundraisingPanelIsRecreatedOnCompanyChange()
    {
        MainWindow.resetSessionForTests(
                new AppPreferencesState(UiThemePreference.SYSTEM_DEFAULT, false, true, UserPrivilegeLevel.ACCOUNTANT),
                new MultiCompanyState("SAMPLE-CO", List.of("SAMPLE-CO")));

        FxTestSupport.onFx(() -> {
            MainWindow window = new MainWindow();
            window.openPanel(AppPanelId.DONORS);
            Object firstCenter = window.panelHostForTests().getCenter();

            window.applyMultiCompany(new MultiCompanyState("SECOND-CO", List.of("SECOND-CO", "SAMPLE-CO")));
            Object secondCenter = window.panelHostForTests().getCenter();

            assertNotSame(firstCenter, secondCenter, "Donors panel should be recreated after company change.");
            return null;
        });
    }

    @Test
    void nonFundraisingPanelRemainsActiveOnDatabaseChange()
    {
        MainWindow.resetSessionForTests(
                new AppPreferencesState(UiThemePreference.SYSTEM_DEFAULT, false, true, UserPrivilegeLevel.ACCOUNTANT),
                new MultiCompanyState("SAMPLE-CO", List.of("SAMPLE-CO")));

        FxTestSupport.onFx(() -> {
            MainWindow window = new MainWindow();
            window.openPanel(AppPanelId.LEDGER_REGISTER);
            Object firstCenter = window.panelHostForTests().getCenter();

            window.applyDatabaseSelection(new DatabaseSelectionState("data/second.mv.db", List.of("data/second.mv.db")));
            Object secondCenter = window.panelHostForTests().getCenter();

            assertSame(firstCenter, secondCenter, "Non-fundraising active panel should not be recreated.");
            return null;
        });
    }
}
