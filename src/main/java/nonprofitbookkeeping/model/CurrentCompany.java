/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CurrentCompany.java
 * CurrentCompany
 */
package nonprofitbookkeeping.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import nonprofitbookkeeping.core.JacksonDataStorer;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;

/**
 * 
 */
public class CurrentCompany
{
	private static Company company;
	private static File currentFile = null;
	private static boolean companyIsOpen = false;
	private static JacksonDataStorer dataStorer = new JacksonDataStorer();
	
	/**  
	 * Constructor CurrentCompany
	 */
	public CurrentCompany()
	{
		company = new Company();
	}
	
	/**
	 * @return the company
	 */
	public static Company getCompany()
	{
		return company;
	}
	
	/**
	 * @param currentFile the currentFile to set
	 */
	public static void setCurrentFile(File currentFile)
	{
		CurrentCompany.currentFile = checkNotNull(currentFile);
	}
	
	/**
	 * @return the currentFile
	 */
	public static File getCurrentFile()
	{
		return CurrentCompany.currentFile;
	}


	/**
	 * Override @see java.lang.Object#toString() 
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
	 * Store back the data to persistent
	 * @throws NoFileCreatedException 
	 * @throws ActionCancelledException 
	 * @throws IOException 
	 */
	public static void persist() throws IOException, ActionCancelledException, NoFileCreatedException
	{
		CurrentCompany.dataStorer.saveData(company,
			checkNotNull(CurrentCompany.currentFile));
		
	}
	
	/**
	 * @param file
	 * @throws NoFileCreatedException 
	 * @throws ActionCancelledException 
	 * @throws IOException 
	 */
	public static void loadFromPersistent(File file) throws IOException, ActionCancelledException, NoFileCreatedException
	{
		CurrentCompany.dataStorer.loadData(Company.class, file);
	}
	/**
	 * 
	 */
	public static void close()
	{
		CurrentCompany.companyIsOpen = false;
		CompanyListener.fireChanged(false);
	}
	
	/**
	 * @return Open(T/F)
	 */
	public static boolean isOpen()
	{
		return CurrentCompany.companyIsOpen;
	}
	
	/**
	 * Mark company as open
	 */
	public static void open()
	{
		CurrentCompany.companyIsOpen = true;
		CompanyListener.fireChanged(true);
	}
	
	public interface CompanyChangeListener extends EventListener
	{
		void companyChange(boolean b);
		
	}
	
	
	public static class CompanyListener
	{
		private final static EventListenerList listeners = new EventListenerList();
		
		public static void addCompanyListener(CompanyChangeListener l)
		{
			CompanyListener.listeners.add(CompanyChangeListener.class, l);
		}
		
		public static void removeCompanyListener(CompanyChangeListener l)
		{
			CompanyListener.listeners.remove(CompanyChangeListener.class, l);
		}
		
		/**
		 * @param b 
		 * 
		 */
		private static void fireChanged(boolean b)
		{
			
			for (CompanyChangeListener l : listeners.getListeners(CompanyChangeListener.class))
			{
				l.companyChange(b);
			}
			
		}
		
	}


	
}
