
package nonprofitbookkeeping.model.ofx;

public class FileUtils
{
	
	/**
	 * Removes the file extension from a given absolute file path.
	 * For example, "/path/to/file.txt" becomes "/path/to/file".
	 *
	 * @param absolutePath the full path to the file
	 * @return the path without the file extension, or the original string if no valid extension is found.
	 */
	public static String stripFileExtension(String absolutePath)
	{
		
		if (absolutePath == null || absolutePath.trim().isEmpty())
		{
			return absolutePath;
		}
		
		// Find the index of the last dot
		int dotIndex = absolutePath.lastIndexOf('.');
		// Find the last file separator (both Unix and Windows)
		int sepIndex = Math.max(absolutePath.lastIndexOf('/'), absolutePath.lastIndexOf('\\'));
		
		// Ensure that the dot is after the last separator (i.e. part of the file name)
		if (dotIndex > sepIndex)
		{
			return absolutePath.substring(0, dotIndex);
		}
		else
		{
			// If no extension is found, return the original path.
			return absolutePath;
		}
		
	}
	
}
