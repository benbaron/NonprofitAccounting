
package nonprofitbookkeeping.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for finding company data files within a specified directory.
 * Company data files are identified by the ".npbk" extension.
 */
public class CompanyLoaderService
{
	/**
	 * Finds all company data files (ending with ".npbk", case-insensitive)
	 * within the specified directory.
	 *
	 * @param dir The directory to search for company files. Must not be null and should be a directory.
	 * @return A {@link List} of {@link File} objects representing the found company files.
	 *         Returns an empty list if the directory does not exist, is not a directory,
	 *         or if no matching files are found.
	 * @throws NullPointerException if {@code dir} is null.
	 */
	public static List<File> findCompanyFiles(File dir)
	{
		if (dir == null) {
            throw new NullPointerException("Directory cannot be null.");
        }
        if (!dir.isDirectory()) {
            return new ArrayList<>(); // Or throw IllegalArgumentException
        }

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
