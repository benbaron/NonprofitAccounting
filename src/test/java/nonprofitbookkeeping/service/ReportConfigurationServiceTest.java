package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.reports.ReportConfiguration;
import nonprofitbookkeeping.ui.helpers.DateSelectionMode; // Corrected import path
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    Path tempDir; // JUnit 5 temporary directory

    @BeforeEach
    void setUp() {
        this.configService = new ReportConfigurationService();
        this.companyDirectory = this.tempDir.toFile(); 
    }

    private ReportConfiguration createSampleConfig(String name) {
        // Use the parameterized constructor which sets the ID
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
        // relativeDateRange is not set by createSampleConfig, so it should be null for both
        assertEquals(expected.getRelativeDateRange(), actual.getRelativeDateRange(), "RelativeDateRange should match.");
    }


    @Test
    void testSaveAndLoad_EmptyList() throws IOException {
        List<ReportConfiguration> emptyList = new ArrayList<>();
        this.configService.saveConfigurations(emptyList, this.companyDirectory);

        File configFile = new File(this.companyDirectory, "report_configurations.json");
        assertTrue(configFile.exists(), "report_configurations.json file should be created.");
        // An empty list results in an empty JSON array "[]"
        assertTrue(Files.readString(configFile.toPath()).trim().equals("[]"), "File content should be an empty JSON array.");


        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertNotNull(loadedConfigs, "Loaded configurations should not be null.");
        assertTrue(loadedConfigs.isEmpty(), "Loaded configurations should be an empty list.");
    }

    @Test
    void testSaveAndLoad_SingleConfiguration() throws IOException {
        ReportConfiguration config1 = createSampleConfig("Monthly Operations Report");
        // The constructor already assigns a UUID to configurationId
        
        List<ReportConfiguration> configsToSave = List.of(config1);
        this.configService.saveConfigurations(configsToSave, this.companyDirectory);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertNotNull(loadedConfigs);
        assertEquals(1, loadedConfigs.size(), "Should load one configuration.");

        assertReportConfigurationsEqual(config1, loadedConfigs.get(0));
    }

    @Test
    void testSaveAndLoad_MultipleConfigurations() throws IOException {
        ReportConfiguration config1 = createSampleConfig("Q1 Financials");
        ReportConfiguration config2 = createSampleConfig("Annual Donor Summary");
        config2.setDateSelectionMode(DateSelectionMode.SINGLE_DATE);
        config2.setSpecificStartDate(null); // For single date mode, start date might be null
        config2.setSpecificEndDate(LocalDate.of(2023,12,31));
        config2.setFundIds(Collections.emptyList()); // No specific funds

        List<ReportConfiguration> configsToSave = List.of(config1, config2);
        this.configService.saveConfigurations(configsToSave, this.companyDirectory);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
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
    void testLoadConfigurations_FileNotFound() {
        // Action: Attempt to load from a directory where the file doesn't exist
        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list if file not found.");
    }

    @Test
    void testLoadConfigurations_CorruptJsonFile() throws IOException {
        File configFile = new File(this.companyDirectory, "report_configurations.json");
        Files.writeString(configFile.toPath(), "{invalid json content,,}");

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list for corrupt JSON.");
        // Log message verification is outside scope of simple unit test assertions
    }
    
    @Test
    void testLoadConfigurations_EmptyJsonFile() throws IOException {
        File configFile = new File(this.companyDirectory, "report_configurations.json");
        Files.writeString(configFile.toPath(), ""); // Empty file

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list for an empty JSON file.");
    }

    @Test
    void testSaveConfigurations_NullCompanyDirectory() {
        ReportConfiguration config = createSampleConfig("Test Config");
        List<ReportConfiguration> configs = List.of(config);
        
        Exception exception = assertThrows(IOException.class, () -> {
            this.configService.saveConfigurations(configs, null);
        });
        assertEquals("Invalid company directory for saving report configurations.", exception.getMessage());
    }

    @Test
    void testSaveConfigurations_InvalidCompanyDirectory() throws IOException {
        ReportConfiguration config = createSampleConfig("Test Config");
        List<ReportConfiguration> configs = List.of(config);
        File testFileAsDir = new File(this.companyDirectory, "not_a_directory.txt");
        assertTrue(testFileAsDir.createNewFile(), "Failed to create test file.");

        Exception exception = assertThrows(IOException.class, () -> {
            this.configService.saveConfigurations(configs, testFileAsDir);
        });
        assertEquals("Invalid company directory for saving report configurations.", exception.getMessage());
        
        assertTrue(testFileAsDir.delete()); // Clean up
    }
    
    @Test
    void testSaveConfigurations_NullConfigsList() throws IOException {
        // Service method logs warning and returns, does not throw, does not create file.
        this.configService.saveConfigurations(null, this.companyDirectory);
        File configFile = new File(this.companyDirectory, "report_configurations.json");
        assertFalse(configFile.exists(), "Config file should not be created for null list.");
    }

    @Test
    void testLoadConfigurations_NullCompanyDirectory() {
        // Service method logs warning and returns empty list.
        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(null);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty());
    }
    
    @Test
    void testLoadConfigurations_InvalidCompanyDirectoryNotADirectory() throws IOException {
        File testFileAsDir = new File(this.companyDirectory, "not_a_directory_for_load.txt");
        assertTrue(testFileAsDir.createNewFile(), "Failed to create test file for loading.");

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(testFileAsDir);
        assertNotNull(loadedConfigs);
        assertTrue(loadedConfigs.isEmpty(), "Should return an empty list if company directory is not a directory.");

        assertTrue(testFileAsDir.delete()); // Clean up
    }

    @Test
    void testLoadConfiguration_WithNullIdInJson() throws IOException {
        // Simulate a JSON file where a configuration object has a null ID
        // (though ReportConfiguration constructor should prevent this on creation)
        String jsonContent = "[{\"configurationId\":null,\"userGivenName\":\"Test Null ID\",\"reportType\":\"balance_sheet\",\"dateSelectionMode\":\"SINGLE_DATE\",\"specificEndDate\":\"2023-12-31\",\"outputFormat\":\"xlsx\"}]";
        File configFile = new File(this.companyDirectory, "report_configurations.json");
        Files.writeString(configFile.toPath(), jsonContent);

        List<ReportConfiguration> loadedConfigs = this.configService.loadConfigurations(this.companyDirectory);
        assertNotNull(loadedConfigs);
        assertEquals(1, loadedConfigs.size());
        ReportConfiguration loadedConfig = loadedConfigs.get(0);
        // The model's constructor sets an ID if null, but Jackson might bypass constructor.
        // The service's load method logs a warning for null/empty ID but doesn't change it.
        assertNull(loadedConfig.getConfigurationId(), "ID should be null as per JSON, and service does not auto-fix on load.");
        assertEquals("Test Null ID", loadedConfig.getUserGivenName());
    }
}
