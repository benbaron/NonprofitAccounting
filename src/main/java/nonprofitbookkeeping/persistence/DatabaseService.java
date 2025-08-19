package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.model.Company;
import java.sql.SQLException;
import java.util.List;

/**
 * High level facade for database persistence.
 * <p>
 * The service exposes simple methods for saving and loading pieces of the
 * domain model without forcing callers to interact with {@link EntityManager}
 * or individual repositories directly.
 * </p>
 */
public class DatabaseService {

    private final CompanyRepository companyRepository = new CompanyRepository();
    private final DatabaseBackupService backupService = new DatabaseBackupService();

    public DatabaseService() {
    }

    /** Persist core parts of the company to the database. */
    public void saveCompany(Company company) {
        if (company == null) {
            return;
        }
        companyRepository.save(company);
    }

    /** Retrieve all companies from the database. */
    public List<Company> listCompanies() {
        return companyRepository.findAll();
    }

    /**
     * Loads a company instance from the database.
     * Currently this reconstructs a {@link Company} with its {@link Ledger}
     * transactions populated from the database.
     */
    public Company loadCompany() {
        List<Company> companies = companyRepository.findAll();
        if (companies.isEmpty()) {
            return new Company();
        }
        return companies.get(0);
    }

    /**
     * Create a SQL backup of the database at the specified path.
     *
     * @param filePath destination for the SQL script
     * @throws SQLException if the backup fails
     */
    public void backupDatabase(String filePath) throws SQLException {
        backupService.backupTo(filePath);
    }

    /**
     * Restore the database from a previously created SQL backup.
     *
     * @param filePath source of the SQL script
     * @throws SQLException if the restore fails
     */
    public void restoreDatabase(String filePath) throws SQLException {
        backupService.restoreFrom(filePath);
    }
}
