package org.nonprofitbookkeeping.model;

import java.util.Locale;

/**
 * Supported persisted visual themes.
 */
public enum UiThemePreference
{
    LIGHT,
    DARK,
    SYSTEM_DEFAULT;

    /**
     * Resolves persisted theme text into a known preference value.
     *
     * @param storedValue persisted theme value (for example {@code dark}, {@code LIGHT}, or {@code system})
     * @return resolved preference, defaulting to {@link #SYSTEM_DEFAULT}
     */
    public static UiThemePreference fromStoredValue(String storedValue)
    {
        if (storedValue == null || storedValue.isBlank())
        {
            return SYSTEM_DEFAULT;
        }

        String normalized = storedValue.trim()
            .replace('-', '_')
            .replace(' ', '_')
            .toUpperCase(Locale.ROOT);

        if ("SYSTEM".equals(normalized))
        {
            return SYSTEM_DEFAULT;
        }

        try
        {
            return UiThemePreference.valueOf(normalized);
        }
        catch (IllegalArgumentException ex)
        {
            return SYSTEM_DEFAULT;
        }
    }
}
