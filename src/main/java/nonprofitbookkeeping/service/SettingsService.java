
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import nonprofitbookkeeping.model.ReportPeriodPreset;
import nonprofitbookkeeping.model.SettingsModel;
import nonprofitbookkeeping.persistence.DocumentRepository;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.MonthDay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nonprofitbookkeeping.util.FormatUtils;

// TODO: Auto-generated Javadoc
/**
 * Service for loading and saving application settings.
 */
public class SettingsService
{
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(SettingsService.class);
	/** Database document name where settings are persisted. */
	private static final String DOCUMENT_NAME = "settings";
	
	/** The Constant MAPPER. */
	private static final ObjectMapper MAPPER = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT);
	
	/** Settings cached in memory. */
	private SettingsModel settings = new SettingsModel();
	
	/**
	 * Returns the current settings instance.
	 *
	 * @return the settings
	 */
	public SettingsModel getSettings()
	{
		return this.settings;
		
	}
	
	/**
	 * Loads settings from the persistent document store.
	 *
	 * @param companyDir retained for backwards compatibility but ignored by the method
	 * @throws IOException if reading from the database fails
	 */
	public void loadSettings(File companyDir) throws IOException
	{
		
		try
		{
			new DocumentRepository().find(DOCUMENT_NAME)
				.ifPresent(payload ->
				{
					
					try
					{
						this.settings =
							MAPPER.readValue(payload, SettingsModel.class);
						LOGGER.info(
							"Settings loaded from database document '{}'.",
							DOCUMENT_NAME);
					}
					catch (IOException ex)
					{
						LOGGER.error(
							"Failed to deserialize settings JSON from database",
							ex);
					}
					
				});
		}
		catch (SQLException e)
		{
			throw new IOException("Failed to load settings from database", e);
		}
		
		applyDefaults();
		syncToPreferences();
		applyCurrencyFormat();
		
	}
	
	/**
	 * Saves current settings to the persistent document store.
	 *
	 * @param companyDir retained for backwards compatibility but ignored by the method
	 * @throws IOException if the database write fails
	 */
	public void saveSettings(File companyDir) throws IOException
	{
		
		syncToPreferences();
		applyCurrencyFormat();
		
		try
		{
			String payload = MAPPER.writeValueAsString(this.settings);
			new DocumentRepository().upsert(DOCUMENT_NAME, payload);
			LOGGER.info("Settings saved to database document '{}'.",
				DOCUMENT_NAME);
		}
		catch (SQLException e)
		{
			throw new IOException("Failed to save settings to database", e);
		}
		
	}
	
	/** Applies sane defaults for optional settings fields. */
	private void applyDefaults()
	{
		SettingsModel m = this.settings;
		
		if (m.getAutosaveIntervalMinutes() <= 0)
		{
			m.setAutosaveIntervalMinutes(5);
		}
		
		if (m.getDefaultCompanyDirectory() == null ||
			m.getDefaultCompanyDirectory().isBlank())
		{
			m.setDefaultCompanyDirectory(
				PreferencesService.getDefaultCompanyDir());
		}
		
		if (m.getLastUsedCompanyFile() == null)
		{
			m.setLastUsedCompanyFile(
				PreferencesService.getLastUsedCompanyFile());
		}
		
		if (m.getDefaultReportPeriod() == null ||
			m.getDefaultReportPeriod().isBlank())
		{
			m.setDefaultReportPeriod(ReportPeriodPreset.YEAR_TO_DATE.name());
		}

		if (m.getTheme() == null || m.getTheme().isBlank())
		{
			m.setTheme(PreferencesService.getThemePreference());
		}

		if (m.getPendingRowTextColor() == null ||
			m.getPendingRowTextColor().isBlank())
		{
			m.setPendingRowTextColor(
				PreferencesService.getPendingRowTextColorPreference());
		}
		
		// options default to true when not specified
		// (Lombok generated getters may return false when null, so no extra
		// handling needed)
	}
	
	/** Keeps the legacy preferences storage in sync with the richer settings model. */
	private void syncToPreferences()
	{
		SettingsModel m = this.settings;
		
		if (m.getDefaultCompanyDirectory() != null &&
			!m.getDefaultCompanyDirectory().isBlank())
		{
			PreferencesService
				.setDefaultCompanyDir(m.getDefaultCompanyDirectory());
		}
		
		if (m.getLastUsedCompanyFile() != null &&
			!m.getLastUsedCompanyFile().isBlank())
		{
			PreferencesService
				.setLastUsedCompanyFile(m.getLastUsedCompanyFile());
		}

		if (m.getTheme() != null && !m.getTheme().isBlank())
		{
			PreferencesService.setThemePreference(m.getTheme());
		}

		if (m.getPendingRowTextColor() != null &&
			!m.getPendingRowTextColor().isBlank())
		{
			PreferencesService
				.setPendingRowTextColorPreference(m.getPendingRowTextColor());
		}
		
	}
	
	/** Updates the shared {@link FormatUtils} formatter to reflect the current settings. */
	private void applyCurrencyFormat()
	{
		String format = this.settings.getCurrencyFormat();
		
		if (format != null && !format.isBlank())
		{
			FormatUtils.setCurrencyFormat(format);
		}
		
	}
	
	/**
	 * Convenience accessor used throughout the UI layer.
	 *
	 * @return the report period preset
	 */
	public ReportPeriodPreset resolveDefaultReportPeriod()
	{
		return ReportPeriodPreset.fromString(
			this.settings.getDefaultReportPeriod(),
			ReportPeriodPreset.YEAR_TO_DATE);
		
	}
	
	/**
	 * Provides the fiscal year start as a {@link MonthDay}.
	 *
	 * @return the month day
	 */
	public MonthDay resolveFiscalYearStart()
	{
		MonthDay parsed = this.settings.getFiscalYearStartMonthDay();
		
		if (parsed != null)
		{
			return parsed;
		}
		
		return MonthDay.of(1, 1);
		
	}
	
}
