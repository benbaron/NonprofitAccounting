/**
 * nonprofit-scaledger-ribbon.zip_expanded CurrentCompany.java CurrentCompany
 */

package nonprofitbookkeeping.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.persistence.DatabaseManager;
import nonprofitbookkeeping.persistence.DatabaseService;
import nonprofitbookkeeping.core.JacksonDataStorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the currently active {@link Company} instance in the application.
 * This class provides static methods to access, load, save, and manage the state
 * of the company data, including its associated file and open status.
 * It also handles listeners for company state changes.
 */
public class CurrentCompany
{
	/** The currently active company instance. */
	private static Company company;
	/** The file associated with the currently open company. Null if no file is open or associated. */
	private static File currentFile = null;
	/** Flag indicating whether a company is currently considered open. */
	private static boolean companyIsOpen = false;
	/** Database service used for loading and saving company data. */
        private static DatabaseService DATABASE_SERVICE = new DatabaseService();

        /** Logger for this class. */
        private static final Logger LOG = LoggerFactory.getLogger(CurrentCompany.class);
	
	/**
	 * Directory where the persistent H2 database files live.
	 * The final database will be ./data/nonprofit.mv.db
	 */
	private static final File H2_DATA_DIR = new File("./data");
	
	/**
	 * JDBC URL for the persistent H2 database.
	 * This matches DatabaseManager and persistence.xml.
	 */
	private static final String H2_JDBC_URL =
		"jdbc:h2:file:./data/nonprofit;AUTO_SERVER=TRUE";
	
	/** Ensures the H2 data directory exists before any connection is opened. */
	private static void ensureDataDir()
	{
		
		if (!H2_DATA_DIR.exists())
		{
			H2_DATA_DIR.mkdirs();
		}
		
	}
	
	/**
	 * Constructs a CurrentCompany manager.
	 * Initializes a new, empty {@link Company} object and sets the company as not open.
	 * Note: While this constructor exists, the class primarily uses static members.
	 * Instantiating CurrentCompany might reset the static company instance if not handled carefully.
	 */
	public CurrentCompany()
	{
		company = new Company();
		companyIsOpen = false;
		
	}
	
	/**
	 * Gets the currently active {@link Company} instance.
	 * @return The current company. Can be a newly initialized company if none has been loaded.
	 */
	public static Company getCompany()
	{
		return company;
		
	}
	
	/**
	 * Sets the file associated with the current company.
	 * @param currentFile The file to associate. Must not be null.
	 * @throws NullPointerException if currentFile is null.
	 */
	public static void setCurrentFile(File currentFile)
	{
		CurrentCompany.currentFile = checkNotNull(currentFile);
		
		// The Company model no longer tracks its backing file directly.
	}
	
	/**
	 * Gets the file associated with the current company.
	 * @return The current file, or null if no file is set.
	 */
	public static File getCurrentFile()
	{
		return CurrentCompany.currentFile;
		
	}
	
	
	/**
	 * Returns a string representation of the CurrentCompany's state,
	 * including the current file and whether a company is open.
	 * @return A string describing the current company state.
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CurrentCompany [currentFile=");
		builder.append(CurrentCompany.currentFile);
		builder.append(", companyIsOpen=");
		builder.append(CurrentCompany.companyIsOpen);
		builder.append("]");
		return builder.toString();
		
	}
	
	/**
	 * Persists the current company data to its associated file.  In normal
	 * operation the application works with <code>.npbk</code> files which are
	 * ZIP archives containing a single {@code company_data.json} entry. This
	 * method serializes the {@link Company} into that ZIP structure via
	 * {@link JacksonDataStorer}. For advanced scenarios a user may instead
	 * select a target file ending in <code>.sql</code>; in that case the
	 * method issues the H2 {@code SCRIPT TO} command to produce a plain-text
	 * dump of the in-memory database.
	 * <p>
	 * Call this before opening another company file if you want to retain the
	 * changes made to the currently active company.
	 * </p>
	 *
	 * @throws IOException if an I/O error occurs during saving.
	 * @throws ActionCancelledException if the save action is cancelled (e.g., by user in a file dialog).
	 * @throws NoFileCreatedException if the file cannot be created or written to.
	 * @throws NullPointerException if the current file has not been set.
	 */
	public static void persist() throws IOException, ActionCancelledException,
		NoFileCreatedException
	{
		DATABASE_SERVICE.saveCompany(company);
		
		if (currentFile == null)
		{
			throw new NoFileCreatedException("No backup file specified");
		}
		
		String name = currentFile.getName().toLowerCase();
		
		if (name.endsWith(".npbk"))
		{
			JacksonDataStorer storer = JacksonDataStorer.getDataStorer();
			storer.saveData(company, currentFile);
			return;
		}
		
		// Export to .sql by scripting the persistent database.
		String path = currentFile.getAbsolutePath().replace("\\", "/");
		String sql = "SCRIPT TO '" + path + "'";
		
		ensureDataDir();
		
		try (
			Connection conn =
				DriverManager.getConnection(H2_JDBC_URL, "sa", "");
			Statement stmt = conn.createStatement())
		{
			stmt.execute(sql);
		}
		catch (SQLException e)
		{
			throw new IOException("Error writing backup: " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * Writes the current {@link Company} state to the embedded database only.
	 * This performs a lightweight persistence step without creating or updating
	 * any backup files. It is intended for scenarios such as closing a company
	 * where changes should be flushed to the local database but no external
	 * export is desired.
	 */
        public static void flushToDatabase()
        {
                if (company == null)
                {
                        LOG.warn("flushToDatabase called but no company is loaded");
                        return;
                }
                LOG.debug("Flushing company '{}' (id: {}) to database", company.getName(),
                        company.getId());
                DATABASE_SERVICE.saveCompany(company);
        }
	
	/**
	* Loads company data from the specified file and makes it the active
	* company.
	* <p>
	* A company is typically stored in a <code>.npbk</code> file created by
	* {@link #persist()}. The file is a ZIP archive containing the
	* {@code company_data.json} entry. When this method is invoked the
	* in-memory database is completely reinitialized and the newly loaded
	* company replaces any previously open company. This mechanism is how the
	* application "switches" between companies when different files are opened.
	* Alternatively, if the supplied file ends with <code>.sql</code>, the
	* method executes H2's {@code RUNSCRIPT FROM} command to rebuild the
	* database from the plain-text dump before reinitializing the application
	* state.
	* </p>
	*
	* @param file The file from which to load company data. Must not be null.
	* @throws IOException if an I/O error occurs during loading.
	* @throws ActionCancelledException if the load action is cancelled.
	* @throws NoFileCreatedException if the file specified does not lead to a
	*         valid company data structure.
	* @throws NullPointerException if file is null.
	*/	
	public static void loadFromPersistent(File file)
		throws IOException, ActionCancelledException,
		NoFileCreatedException
	{
		checkNotNull(file, "File cannot be null for load operation.");
		
		String name = file.getName().toLowerCase();
		
		if (name.endsWith(".npbk") || isZipArchive(file))
		{
			JacksonDataStorer storer = JacksonDataStorer.getDataStorer();
			Company loaded = storer.loadData(Company.class, file);
			
			DatabaseManager.shutdown();
			DatabaseManager.initialize();
			DATABASE_SERVICE = new DatabaseService();
			DATABASE_SERVICE.saveCompany(loaded);
			
			company = loaded;
			setCurrentFile(file);
			
			if (company != null)
			{
				markCompanyOpen();
			}
			
			return;
		}
		
		String path = file.getAbsolutePath().replace("\\", "/");
		String sql = "RUNSCRIPT FROM '" + path + "'";
		
		try (
			Connection conn =
				DriverManager.getConnection("jdbc:h2:mem:nonprofit", "sa", "");
			Statement stmt = conn.createStatement())
		{
			stmt.execute(sql);
		}
		catch (SQLException e)
		{
			throw new IOException("Error restoring backup: " + e.getMessage(),
				e);
		}
		
		DatabaseManager.shutdown();
		DatabaseManager.initialize();
		DATABASE_SERVICE = new DatabaseService();
		
		company = DATABASE_SERVICE.loadCompany();
		
		setCurrentFile(file);
		
		if (company != null)
		{
			markCompanyOpen();
		}
		
	}
	
	/**
	 * Determines if the given file is a ZIP archive by checking its magic header.
	 *
	 * @param file the file to inspect
	 * @return {@code true} if the file appears to be a ZIP archive, {@code false} otherwise
	 */
	private static boolean isZipArchive(File file)
	{
		
		try (FileInputStream fis = new FileInputStream(file))
		{
			byte[] header = new byte[4];
			
			if (fis.read(header) < 4)
			{
				return false;
			}
			
			return header[0] == 'P' && header[1] == 'K';
		}
		catch (IOException ex)
		{
			return false;
		}
		
	}
	
	
	/**
	 * Reload the company data directly from the database.
	 * This bypasses any file-based migration and simply loads the
	 * first stored {@link Company} from the database into the
	 * current context.
	 */
        public static void loadFromDatabase()
        {
                try
                {
                        company = DATABASE_SERVICE.loadCompany();

                        if (company != null)
                        {
                                LOG.info("Loaded company '{}' (id: {}) from database",
                                        company.getName(), company.getId());
                                markCompanyOpen();
                        }
                        else
                        {
                                LOG.info("No company found in database");
                        }
                }
                catch (Exception e)
                {
                        LOG.error("Failed to load company from database", e);
                }
        }
	
	
	/**
	 * Closes the currently open company.
	 * Sets the company open status to false and notifies listeners.
	 * Does not clear the company data or current file itself, only marks as closed.
	 */
	public static void close()
	{
		CurrentCompany.companyIsOpen = false;
		CompanyListener.fireChanged(false);
		
	}
	
	/**
	 * Checks if a company is currently considered open.
	 * @return {@code true} if a company is open, {@code false} otherwise.
	 */
	public static boolean isOpen()
	{
		return CurrentCompany.companyIsOpen;
		
	}
	
	/**
	 * Marks the current company as open and notifies listeners.
	 * <p>
	 * This method does <strong>not</strong> load any company data. It merely
	 * updates the "company is open" flag and fires a change event so that
	 * UI panels can refresh themselves. It is typically called after the
	 * company has been loaded from disk.
	 * </p>
	 */
	public static void markCompanyOpen()
	{
		CurrentCompany.companyIsOpen = true;
		CompanyListener.fireChanged(true);
		
	}
	
	/**
	 * Listener interface for receiving notifications about company state changes (e.g., opened or closed).
	 */
	public interface CompanyChangeListener extends EventListener
	{
		/**
		 * Invoked when the company's open/closed state changes.
		 * @param isOpen {@code true} if the company is now open, {@code false} if it is now closed.
		 */
		void companyChange(boolean isOpen);
		
	}
	
	/**
	 * Manages a list of {@link CompanyChangeListener}s and fires events to them.
	 */
	public static class CompanyListener
	{
		/** List of registered listeners. */
		private final static EventListenerList listeners =
			new EventListenerList();
		
		/**
		 * Adds a {@link CompanyChangeListener} to the listener list.
		 * @param l The listener to add.
		 */
		public static void addCompanyListener(CompanyChangeListener l)
		{
			CompanyListener.listeners.add(CompanyChangeListener.class, l);
			
		}
		
		/**
		 * Removes a {@link CompanyChangeListener} from the listener list.
		 * @param l The listener to remove.
		 */
		public static void removeCompanyListener(CompanyChangeListener l)
		{
			CompanyListener.listeners.remove(CompanyChangeListener.class, l);
			
		}
		
		/**
		 * Returns the list of currently registered {@link CompanyChangeListener}s.
		 *
		 * <p>This method exposes the listeners primarily for testing
		 * purposes so that UI tests can manually trigger company change
		 * events. The returned list is a snapshot &ndash; modifications
		 * to it will <strong>not</strong> affect the internal listener
		 * registry.</p>
		 *
		 * @return an immutable {@link List} of registered listeners, or
		 *         an empty list if none are registered
		 */
		public static List<CompanyChangeListener> getListeners()
		{
			CompanyChangeListener[] arr =
				CompanyListener.listeners
					.getListeners(CompanyChangeListener.class);
			return List.of(arr);
			
		}
		
		/**
		 * Notifies all registered {@link CompanyChangeListener}s that the company open state changed.
		 * @param isOpen {@code true} if the company is now open, {@code false} otherwise.
		 */
		public static void fireChanged(boolean isOpen)
		{
			
			for (CompanyChangeListener l : CompanyListener.getListeners())
			{
				l.companyChange(isOpen);
			}
			
		}
		
	}
	
	/**
	 * For test environments or specialized workflows where the caller needs
	 * to directly set the current {@link Company} and immediately notify all
	 * {@link CompanyChangeListener}s, this helper provides that ability.
	 *
	 * <p>If {@code company2} is {@code null} the current company reference is
	 * cleared and listeners are notified as if the company was closed.
	 * Otherwise the provided company becomes the active company and listeners
	 * are notified that a company is open.</p>
	 *
	 * @param company2 the company to set as the current one, or {@code null}
	 *                 to simulate closing the current company
	 */
	public static void forceCompanyLoad(Company company2)
	{
		CurrentCompany.company = company2;
		
		if (company2 != null)
		{
			CurrentCompany.companyIsOpen = true;
			CompanyListener.fireChanged(true);
		}
		else
		{
			CurrentCompany.companyIsOpen = false;
			CompanyListener.fireChanged(false);
		}
		
	}
	
}
