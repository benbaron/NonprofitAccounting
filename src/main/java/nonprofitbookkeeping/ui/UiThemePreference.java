package nonprofitbookkeeping.ui;

/**
 * Supported persisted visual themes.
 */
public enum UiThemePreference
{
	LIGHT("Light"),
	DARK("Dark"),
	SYSTEM_DEFAULT("System");

	private final String displayName;

	UiThemePreference(String displayName)
	{
		this.displayName = displayName;
	}

	public String displayName()
	{
		return this.displayName;
	}

	public String persistedValue()
	{
		return name();
	}

	public static UiThemePreference fromStoredValue(String value)
	{
		if (value == null || value.isBlank())
		{
			return SYSTEM_DEFAULT;
		}

		String normalized = value.trim();

		for (UiThemePreference candidate : values())
		{
			if (candidate.displayName.equalsIgnoreCase(normalized)
				|| candidate.name().equalsIgnoreCase(normalized))
			{
				return candidate;
			}
		}

		return SYSTEM_DEFAULT;
	}

	@Override
	public String toString()
	{
		return this.displayName;
	}
}
