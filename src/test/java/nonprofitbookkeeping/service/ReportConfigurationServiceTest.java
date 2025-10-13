package nonprofitbookkeeping.service;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.persistence.JsonStorageRepository;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportConfigurationServiceTest {

    private ReportConfigurationService configService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        TestDatabase.reset(this.tempDir);
        this.configService = new ReportConfigurationService();
    }

    private ReportConfiguration createSampleConfig(String name) {
        return new ReportConfiguration(
            name,
            "income_statement",
            DateSelectionMode.DATE_RANGE_MANDATORY_START,
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 1, 31),
            Arrays.asList("FUND_A_NAME", "FUND_B_NAME")
        );
    }

    private void assertReportConfigurationsEqual(ReportConfiguration expected, ReportConfiguration actual) {
        assertNotNull(actual, "Loaded configuration should not be null.");
        assertEquals(expected.getConfigurationId(), actual.getConfigurationId(), "Configuration ID should match.");
        assertEquals(expected.getUserGivenName(), actual.getUserGivenName(), "UserGivenName should match.");
        assertEquals(expected.getReportType(), actual.getReportType(), "ReportType should match.");
        assertEquals(expected.getDateSelectionMode(), actual.getDateSelectionMode(), "DateSelectionMode should match.");
        assertEquals(expected.getSpecificStartDate(), actual.getSpecificStartDate(), "SpecificStartDate should match.");
        assertEquals(expected.getSpecificEndDate(), actual.getSpecificEndDate(), "SpecificEndDate should match.");
        assertEquals(expected.getFundIds(), actual.getFundIds(), "FundIds should match.");
        assertEquals(expected.getOutputFormat(), actual.getOutputFormat(), "OutputFormat should match.");
        assertEquals(expected.getRelativeDateRange(), actual.getRelativeDateRange(), "RelativeDateRange should match.");
    }

    @Test
    void testSaveAndLoad_EmptyList() throws IOException {
        List<ReportConfiguration> emptyList = new ArrayList<>();
        this.configService.saveConfigurations(emptyList, null);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertNotNull(loadedConfigs, "Loaded configurations should not be null.");
        assertTrue(loadedConfigs.isEmpty(), "Loaded configurations should be an empty list.");
    }

    @Test
    void testSaveAndLoad_SingleConfiguration() throws IOException {
        ReportConfiguration config1 = createSampleConfig("Monthly Operations Report");

        List<ReportConfiguration> configsToSave = List.of(config1);
        this.configService.saveConfigurations(configsToSave, null);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertNotNull(loadedConfigs);
        assertEquals(1, loadedConfigs.size(), "Should load one configuration.");

        assertReportConfigurationsEqual(config1, loadedConfigs.get(0));
    }

    @Test
    void testSaveAndLoad_MultipleConfigurations() throws IOException {
        ReportConfiguration config1 = createSampleConfig("Q1 Financials");
        ReportConfiguration config2 = createSampleConfig("Annual Donor Summary");
        config2.setDateSelectionMode(DateSelectionMode.SINGLE_DATE);
        config2.setSpecificStartDate(null);
        config2.setSpecificEndDate(LocalDate.of(2023,12,31));
        config2.setFundIds(Collections.emptyList());

        List<ReportConfiguration> configsToSave = List.of(config1, config2);
        this.configService.saveConfigurations(configsToSave, null);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertNotNull(loadedConfigs);
        assertEquals(2, loadedConfigs.size(), "Should load two configurations.");

        ReportConfiguration loadedConfig1 = loadedConfigs.stream()
            .filter(c -> c.getConfigurationId().equals(config1.getConfigurationId())).findFirst().orElse(null);
        ReportConfiguration loadedConfig2 = loadedConfigs.stream()
            .filter(c -> c.getConfigurationId().equals(config2.getConfigurationId())).findFirst().orElse(null);

        assertReportConfigurationsEqual(config1, loadedConfig1);
        assertReportConfigurationsEqual(config2, loadedConfig2);
    }

    @Test
    void testLoadConfigurations_NoData() {
        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list if nothing stored.");
    }

    @Test
    void testLoadConfigurations_CorruptJsonPayload() throws SQLException {
        new JsonStorageRepository().save("report_configurations", "{invalid json content,,}");

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list for corrupt JSON.");
    }

    @Test
    void testSaveConfigurations_NullListClearsStorage() throws IOException {
        ReportConfiguration config = createSampleConfig("Test Config");
        this.configService.saveConfigurations(List.of(config), null);
        this.configService.saveConfigurations(null, null);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertTrue(loadedConfigs.isEmpty());
    }

    @Test
    void testLoadConfiguration_WithNullIdInPayload() throws SQLException {
        String jsonContent = "[{\"configurationId\":null,\"userGivenName\":\"Test Null ID\",\"reportType\":\"balance_sheet\",\"dateSelectionMode\":\"SINGLE_DATE\",\"specificEndDate\":\"2023-12-31\",\"outputFormat\":\"xlsx\"}]";
        new JsonStorageRepository().save("report_configurations", jsonContent);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertNotNull(loadedConfigs);
        assertEquals(1, loadedConfigs.size());
        ReportConfiguration loadedConfig = loadedConfigs.get(0);
        assertNull(loadedConfig.getConfigurationId());
        assertEquals("Test Null ID", loadedConfig.getUserGivenName());
    }

    @Test
    void testLoadConfigurations_IgnoresDirectoryParameter() {
        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(new java.io.File("irrelevant"));
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty());
    }
}
