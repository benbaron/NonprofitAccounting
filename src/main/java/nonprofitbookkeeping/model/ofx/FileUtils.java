
package nonprofitbookkeeping.model.ofx;

/**
 * Utility class for file-related operations.
 * This class provides static helper methods for common file manipulation tasks.
 */
public class FileUtils
{
	
	/**
	 * Removes the file extension from a given absolute file path.
	 * For example, "/path/to/file.txt" becomes "/path/to/file".
	 * If the path is null, empty, or does not contain a valid extension
	 * (i.e., a dot in the filename part), the original path is returned.
	 *
	 * @param absolutePath The full path to the file.
	 * @return The path without the file extension, or the original string if no valid extension is found or the input is invalid.
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
