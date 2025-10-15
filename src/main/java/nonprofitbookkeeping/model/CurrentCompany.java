/**
 * nonprofit-scaledger-ribbon.zip_expanded CurrentCompany.java CurrentCompany
 */

package nonprofitbookkeeping.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import nonprofitbookkeeping.persistence.CompanyDataRepository;
import nonprofitbookkeeping.persistence.CompanyRepository;

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
        /** Flag indicating whether a company is currently considered open. */
        private static boolean companyIsOpen = false;
        /** Identifier of the persisted company row. */
        private static Long currentCompanyId = null;
        /** Repository used to read/write company aggregates from the database. */
        private static final CompanyRepository repository = new CompanyRepository();
        /** Repository that synchronizes normalized company data tables. */
        private static final CompanyDataRepository dataRepository = new CompanyDataRepository();
	
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
	 * Returns a string representation of the CurrentCompany's state,
	 * including the current file and whether a company is open.
	 * @return A string describing the current company state.
	 */
	@Override public String toString()
	{
		StringBuilder builder = new StringBuilder();
                builder.append("CurrentCompany [companyId=");
                builder.append(CurrentCompany.currentCompanyId);
                builder.append(", companyIsOpen=");
		builder.append(CurrentCompany.companyIsOpen);
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Persists the current company data to the database using the {@link CompanyRepository}.
	 * When the company has not yet been saved a new row is created and its identifier is tracked
	 * for subsequent saves.
	 *
	 * @throws IOException if serialization or the database write fails
	 */
	public static void persist() throws IOException
	{
		try
		{
                        Company companyToPersist = checkNotNull(company,
                                "Company must not be null when persisting");
                        dataRepository.persist(companyToPersist);
                        long id = repository.save(CurrentCompany.currentCompanyId, companyToPersist);
                        CurrentCompany.currentCompanyId = id;
                }
                catch (IOException e)
                {
                        throw e;
                }
                catch (Exception e)
                {
                        throw new IOException("Failed to persist company", e);
                }
		
	}
	
	/**
	 * Loads company data from the database and makes it the current active company.
	 *
	 * @param companyId identifier of the stored company
	 * @throws IOException if the company cannot be retrieved or deserialized
	 */
	public static void loadFromPersistent(long companyId) throws IOException
	{
		try
		{
                        Company normalized = dataRepository.load();
                        boolean hasNormalizedData = normalized != null
                                && (!normalized.getChartOfAccounts().getAccounts().isEmpty()
                                        || !normalized.getLedger().getJournal().getJournalTransactions().isEmpty()
                                        || normalized.getCompanyProfileModel() != null);

                        if (!hasNormalizedData)
                        {
                                company = repository.load(companyId);
                                dataRepository.persist(company);
                        }
                        else
                        {
                                Company legacy = repository.load(companyId);

                                if (legacy != null)
                                {
                                        normalized.setCompanyFile(legacy.getCompanyFile());
                                }

                                company = normalized;
                        }

                        CurrentCompany.currentCompanyId = companyId;
                        if (company != null)
                        {
                                markCompanyOpen();
                        }
                }
                catch (IOException e)
                {
                        throw e;
                }
		catch (Exception e)
		{
			throw new IOException("Failed to load company", e);
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
		CurrentCompany.currentCompanyId = null;
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
		CurrentCompany.currentCompanyId = null;
		
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
	
	/**
	 * Convenience helper that sets both the active company identifier and the aggregate.
	 */
	public static void forceCompanyLoad(Long companyId, Company company2)
	{
		CurrentCompany.currentCompanyId = companyId;
		forceCompanyLoad(company2);
	}

	/** Returns the identifier of the currently open company, or {@code null} if unsaved. */
	public static Long getCurrentCompanyId()
	{
		return CurrentCompany.currentCompanyId;
	}

}
