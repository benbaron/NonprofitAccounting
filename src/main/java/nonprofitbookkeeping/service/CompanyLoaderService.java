
package nonprofitbookkeeping.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompanyLoaderService
{
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
	

}
