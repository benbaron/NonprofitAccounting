package org.nonprofitbookkeeping.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.nonprofitbookkeeping.ui.AlternateSettingsValidator;

class AlternateSettingsValidatorTest
{
    @Test
    void acceptsValidFiscalYearStartAndConfiguredAccounts()
    {
        List<String> errors = AlternateSettingsValidator.validate("04-01", "4000 — Donations", "5000 — Supplies",
            List.of("4000 — Donations", "5000 — Supplies"));

        assertTrue(errors.isEmpty());
    }

    @Test
    void rejectsInvalidFiscalYearStart()
    {
        List<String> errors = AlternateSettingsValidator.validate("02-31", "", "", List.of());

        assertEquals(List.of("Fiscal year start must be a valid MM-DD value."), errors);
    }

    @Test
    void rejectsDefaultAccountsThatAreNotActivePostingAccounts()
    {
        List<String> errors = AlternateSettingsValidator.validate("01-01", "9999 — Missing", "5000 — Supplies",
            List.of("4000 — Donations"));

        assertEquals(List.of(
            "Default income account must be one of the active posting accounts.",
            "Default expense account must be one of the active posting accounts."), errors);
    }
}
