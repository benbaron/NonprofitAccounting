
package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.exception.NoFileException;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompanyLoaderService
{
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Find company files
	 * @param dir
	 * @return list of files
	 */
	public static List<File> findCompanyFiles(File dir)
	{
		File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".npbk"));
		List<File> result = new ArrayList<>();
		
		if (files != null)
		{
			
			for (File f : files)
			{
				result.add(f);
			}
			
		}
		
		return result;
	}
	
	/**
	 * Load company profile
	 * @param file
	 * @return
	 * @throws NoFileException 
	 */
	public static Company loadCompanyProfile(File file) throws NoFileException
	{
		
		try
		{
			Company cdf = mapper.readValue(file, Company.class);
			return cdf;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new NoFileException("Cannot load company profile");
		}
		
	}
	
	/**
	 * Save Company File
	 * @param file
	 * @param profile
	 * @return
	 */
	public static boolean saveCompanyProfile(File file, CompanyProfileModel profile)
	{
		Company.getCompany().setCompanyProfileModel(profile);
		return true;
	}
	
	/**
	 * Load Company Data
	 * @param file
	 * @return
	 */
	public static Ledger loadCompanyData(File file)
	{
		
		try
		{
			return mapper.readValue(file, Ledger.class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * Save Company Data
	 * @param file
	 * @param data
	 * @return
	 */
	public static boolean saveCompanyData(File file, Ledger data)
	{
		
		try
		{
			mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
	}
	

	
}
