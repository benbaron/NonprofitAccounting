package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.CurrentCompany;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.model.AppPreferencesState;
import org.nonprofitbookkeeping.model.MultiCompanyState;
import org.nonprofitbookkeeping.model.UiThemePreference;
import org.nonprofitbookkeeping.model.UserPrivilegeLevel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainWindowCompanyLifecycleTest
{
    @BeforeAll
    static void setupFx()
    {
        FxTestSupport.initToolkitOrSkip();
    }

    @Test
    void selectingCompanyMarksCurrentCompanyOpen()
    {
        MainWindow.resetSessionForTests(
                new AppPreferencesState(UiThemePreference.SYSTEM_DEFAULT, false, true, UserPrivilegeLevel.ACCOUNTANT),
                new MultiCompanyState("SAMPLE-CO", List.of("SAMPLE-CO")));

        FxTestSupport.onFx(() -> {
            MainWindow window = new MainWindow();
            window.selectCompanyForTests("BETA-CO");
            assertTrue(CurrentCompany.isOpen());
            return null;
        });
    }

    @Test
    void closingCompanyMarksCurrentCompanyClosed()
    {
        MainWindow.resetSessionForTests(
                new AppPreferencesState(UiThemePreference.SYSTEM_DEFAULT, false, true, UserPrivilegeLevel.ACCOUNTANT),
                new MultiCompanyState("SAMPLE-CO", List.of("SAMPLE-CO")));

        FxTestSupport.onFx(() -> {
            MainWindow window = new MainWindow();
            window.selectCompanyForTests("BETA-CO");
            window.closeCompanyForTests();
            assertFalse(CurrentCompany.isOpen());
            return null;
        });
    }
}
