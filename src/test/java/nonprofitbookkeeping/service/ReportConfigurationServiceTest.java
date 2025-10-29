package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.persistence.DocumentRepository;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportConfigurationServiceTest {

    private ReportConfigurationService configService;
    private File companyDirectory;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Path dbFile = this.tempDir.resolve("report-config-db");
        Database.init(dbFile);
        Database.get().ensureSchema();
        this.configService = new ReportConfigurationService();
        this.companyDirectory = this.tempDir.toFile();
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
        assertEquals(expected.getConfigurationId(), actual.getConfigurationId());
        assertEquals(expected.getUserGivenName(), actual.getUserGivenName());
        assertEquals(expected.getReportType(), actual.getReportType());
        assertEquals(expected.getDateSelectionMode(), actual.getDateSelectionMode());
        assertEquals(expected.getSpecificStartDate(), actual.getSpecificStartDate());
        assertEquals(expected.getSpecificEndDate(), actual.getSpecificEndDate());
        assertEquals(expected.getFundIds(), actual.getFundIds());
        assertEquals(expected.getOutputFormat(), actual.getOutputFormat());
    }

    @Test
    void testSaveAndLoad_EmptyList() throws IOException {
        List<ReportConfiguration> emptyList = new ArrayList<>();
        this.configService.saveConfigurations(emptyList, this.companyDirectory);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
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
        config2.setSpecificEndDate(LocalDate.of(2023, 12, 31));
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
        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list if nothing was saved.");
    }

    @Test
    void testLoadConfigurations_CorruptJsonInDatabase() throws IOException {
        try {
            new DocumentRepository().upsert("report_configurations", "{invalid json content,,}");
        } catch (Exception e) {
            throw new IOException(e);
        }

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list for corrupt JSON.");
    }

    @Test
    void testLoadConfigurations_EmptyJsonInDatabase() throws IOException {
        try {
            new DocumentRepository().upsert("report_configurations", "");
        } catch (Exception e) {
            throw new IOException(e);
        }

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list for an empty JSON payload.");
    }

    @Test
    void testSaveConfigurations_NullCompanyDirectory() throws IOException {
        ReportConfiguration config = createSampleConfig("Test Config");
        List<ReportConfiguration> configs = List.of(config);

        assertDoesNotThrow(() -> this.configService.saveConfigurations(configs, null));
    }

    @Test
    void testSaveConfigurations_NullListClearsStorage() throws IOException {
        ReportConfiguration config = createSampleConfig("Test Config");
        List<ReportConfiguration> configs = List.of(config);
        File testFileAsDir = new File(this.companyDirectory, "not_a_directory.txt");
        assertTrue(testFileAsDir.createNewFile(), "Failed to create test file.");

        assertDoesNotThrow(() -> this.configService.saveConfigurations(configs, testFileAsDir));
    }

    @Test
    void testSaveConfigurations_NullConfigsList() throws IOException {
        this.configService.saveConfigurations(null, this.companyDirectory);
        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertTrue(loadedConfigs.isEmpty());
    }

    @Test
    void testLoadConfigurations_NullCompanyDirectory() {
        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertTrue(loadedConfigs.isEmpty());
    }
}
