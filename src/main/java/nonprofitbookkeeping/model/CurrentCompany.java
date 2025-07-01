/**
 * nonprofit-scaledger-ribbon.zip_expanded CurrentCompany.java CurrentCompany
 */

package nonprofitbookkeeping.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

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
	/** DataStorer instance used for loading and saving company data. */
	private static JacksonDataStorer dataStorer = new JacksonDataStorer();
	
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
		
		// Keep the Company object's file reference in sync with the
		// static currentFile value. This ensures callers querying
		// {@link Company#getCompanyFile()} receive the correct
		// location after load/save operations.
		if (company != null)
		{
			company.setCompanyFile(CurrentCompany.currentFile);
		}
		
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
	@Override public String toString()
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
	 * Persists the current company data to its associated file.
	 * Uses the configured {@link JacksonDataStorer} to save the data.
	 * 
	 * @throws IOException if an I/O error occurs during saving.
	 * @throws ActionCancelledException if the save action is cancelled (e.g., by user in a file dialog).
	 * @throws NoFileCreatedException if the file cannot be created or written to.
	 * @throws NullPointerException if the current file has not been set.
	 */
	public static void persist()	throws IOException, ActionCancelledException,
									NoFileCreatedException
	{
		CurrentCompany.dataStorer.saveData(
			company,
			checkNotNull(CurrentCompany.currentFile,
				"Current file cannot be null for persist operation."));
		
	}
	
	/**
	 * Loads company data from the specified file.
	 * The loaded company becomes the current active company, and the specified file becomes the current file.
	 * @param file The file from which to load company data. Must not be null.
	 * @throws IOException if an I/O error occurs during loading.
	 * @throws ActionCancelledException if the load action is cancelled.
	 * @throws NoFileCreatedException if the file specified does not lead to a valid company data structure.
	 * @throws NullPointerException if file is null.
	 */
	public static void loadFromPersistent(File file)	throws IOException, ActionCancelledException,
														NoFileCreatedException
	{
		company = CurrentCompany.dataStorer.loadData(
			Company.class,
			checkNotNull(file, "File cannot be null for load operation."));
		setCurrentFile(file);
		
		// Ensure the loaded company object stores its file reference.
		if (company != null)
		{
			company.setCompanyFile(file);
			markCompanyOpen();
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
		private final static EventListenerList listeners = new EventListenerList();
		
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
				CompanyListener.listeners.getListeners(CompanyChangeListener.class);
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
