package nonprofitbookkeeping.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiThemePreferenceTest
{
	@Test
	void fromStoredValueAcceptsLegacyDisplayNames()
	{
		assertEquals(UiThemePreference.DARK, UiThemePreference.fromStoredValue("Dark"));
		assertEquals(UiThemePreference.LIGHT, UiThemePreference.fromStoredValue("Light"));
		assertEquals(UiThemePreference.SYSTEM_DEFAULT, UiThemePreference.fromStoredValue("System"));
	}

	@Test
	void fromStoredValueAcceptsEnumNamesAndDefaultsSafely()
	{
		assertEquals(UiThemePreference.DARK, UiThemePreference.fromStoredValue("DARK"));
		assertEquals(UiThemePreference.DARK, UiThemePreference.fromStoredValue(" dark "));
		assertEquals(UiThemePreference.SYSTEM_DEFAULT, UiThemePreference.fromStoredValue(""));
		assertEquals(UiThemePreference.SYSTEM_DEFAULT, UiThemePreference.fromStoredValue("unexpected"));
	}

	@Test
	void persistedValueUsesStableEnumName()
	{
		assertEquals("DARK", UiThemePreference.DARK.persistedValue());
	}
}
