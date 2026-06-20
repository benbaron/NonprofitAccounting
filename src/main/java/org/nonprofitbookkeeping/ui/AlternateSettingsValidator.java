package org.nonprofitbookkeeping.ui;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Validation rules shared by the native alternate settings panel and tests. */
public final class AlternateSettingsValidator
{
    private static final DateTimeFormatter FISCAL_START_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    private AlternateSettingsValidator() {}

    public static List<String> validate(String fiscalYearStart, String defaultIncomeAccount,
        String defaultExpenseAccount, Collection<String> validAccountKeys)
    {
        List<String> errors = new ArrayList<>();
        if (fiscalYearStart != null && !fiscalYearStart.isBlank())
        {
            try
            {
                MonthDay.parse(fiscalYearStart.trim(), FISCAL_START_FORMAT);
            }
            catch (DateTimeParseException ex)
            {
                errors.add("Fiscal year start must be a valid MM-DD value.");
            }
        }
        validateAccount("Default income account", defaultIncomeAccount, validAccountKeys, errors);
        validateAccount("Default expense account", defaultExpenseAccount, validAccountKeys, errors);
        return errors;
    }

    private static void validateAccount(String label, String account, Collection<String> validAccountKeys, List<String> errors)
    {
        if (account == null || account.isBlank())
        {
            return;
        }
        if (validAccountKeys == null || !validAccountKeys.contains(account))
        {
            errors.add(label + " must be one of the active posting accounts.");
        }
    }
}
