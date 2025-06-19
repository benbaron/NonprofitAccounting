package nonprofitbookkeeping.service;

<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import jakarta.persistence.EntityManager;
=======
>>>>>>> b1f07f2 Extend SQL support
import nonprofitbookkeeping.model.reports.ReportConfiguration;
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.persistence.dao.ReportConfigurationDao;
=======
import nonprofitbookkeeping.ui.helpers.DateSelectionMode;
>>>>>>> b1f07f2 Extend SQL support

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for managing {@link ReportConfiguration} objects.
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file
 * <p>
 * Previous iterations persisted configurations to JSON on disk using
 * {@code JacksonDataStorer}. This implementation delegates persistence to the
 * JPA layer through {@link ReportConfigurationDao} so configurations are stored
 * in the embedded database.
 * </p>
=======
 * Data is persisted using the embedded SQL database via {@link DatabaseManager}.
>>>>>>> b1f07f2 Extend SQL support
 */
public class ReportConfigurationService {
<<<<<<< Upstream, based on origin/codex/read-provided-xlsx-file

    /** Logger for this class. */
    private static final Logger LOGGER =
            Logger.getLogger(ReportConfigurationService.class.getName());

    public ReportConfigurationService() {
    }

    /**
     * Persist the provided report configurations. The {@code companyDirectory}
     * parameter is retained for API compatibility but is no longer used.
     */
    public void saveConfigurations(List<ReportConfiguration> configs,
                                   File companyDirectory) throws IOException {
        if (configs == null) {
            LOGGER.warning("Attempted to save a null list of report configurations. Aborting.");
            return;
        }
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            ReportConfigurationDao dao = new ReportConfigurationDao(em);
            dao.saveAll(configs);
        }
    }

    /**
     * Load all saved report configurations from the database. The
     * {@code companyDirectory} parameter is ignored but kept for backward
     * compatibility.
     */
    public List<ReportConfiguration> loadConfigurations(File companyDirectory) {
        try (EntityManager em = DatabaseManager.getEntityManager()) {
            ReportConfigurationDao dao = new ReportConfigurationDao(em);
            return dao.findAll();
        } catch (Exception e) {
            LOGGER.warning("Error loading report configurations: " + e.getMessage());
            return new ArrayList<>();
        }
=======
    private static final Logger LOGGER = Logger.getLogger(ReportConfigurationService.class.getName());

    /** Constructs a new service instance. */
    public ReportConfigurationService() {
    }

    /**
     * Saves the provided configurations to the database. Existing rows with the
     * same configuration ID will be replaced.
     */
    public void saveConfigurations(List<ReportConfiguration> configs, File companyDirectory) throws IOException {
        if (configs == null) {
            LOGGER.warning("Attempted to save a null list of report configurations. Aborting.");
            return;
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "MERGE INTO report_configuration(configuration_id,user_given_name,report_type,date_selection_mode,relative_date_range,specific_start_date,specific_end_date,fund_ids,output_format,account_ids) KEY(configuration_id) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
            for (ReportConfiguration rc : configs) {
                ps.setString(1, rc.getConfigurationId());
                ps.setString(2, rc.getUserGivenName());
                ps.setString(3, rc.getReportType());
                ps.setString(4, rc.getDateSelectionMode() == null ? null : rc.getDateSelectionMode().name());
                ps.setString(5, rc.getRelativeDateRange());
                ps.setDate(6, rc.getSpecificStartDate() == null ? null : Date.valueOf(rc.getSpecificStartDate()));
                ps.setDate(7, rc.getSpecificEndDate() == null ? null : Date.valueOf(rc.getSpecificEndDate()));
                ps.setString(8, listToString(rc.getFundIds()));
                ps.setString(9, rc.getOutputFormat());
                ps.setString(10, listToString(rc.getAccountIdsForDetailReport()));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving report configurations", e);
        }
    }

    /**
     * Loads all report configurations from the database.
     */
    public List<ReportConfiguration> loadConfigurations(File companyDirectory) {
        List<ReportConfiguration> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT configuration_id,user_given_name,report_type,date_selection_mode,relative_date_range,specific_start_date,specific_end_date,fund_ids,output_format,account_ids FROM report_configuration")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ReportConfiguration rc = new ReportConfiguration();
                rc.setConfigurationId(rs.getString(1));
                rc.setUserGivenName(rs.getString(2));
                rc.setReportType(rs.getString(3));
                String mode = rs.getString(4);
                if (mode != null) rc.setDateSelectionMode(DateSelectionMode.valueOf(mode));
                rc.setRelativeDateRange(rs.getString(5));
                Date sd = rs.getDate(6);
                if (sd != null) rc.setSpecificStartDate(sd.toLocalDate());
                Date ed = rs.getDate(7);
                if (ed != null) rc.setSpecificEndDate(ed.toLocalDate());
                rc.setFundIds(stringToList(rs.getString(8)));
                rc.setOutputFormat(rs.getString(9));
                rc.setAccountIdsForDetailReport(stringToList(rs.getString(10)));
                list.add(rc);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading report configurations", e);
        }
        return list;
    }

    private static String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private static List<String> stringToList(String s) {
        if (s == null || s.isEmpty()) return new ArrayList<>();
        return Arrays.asList(s.split(","));
>>>>>>> b1f07f2 Extend SQL support
    }
}
