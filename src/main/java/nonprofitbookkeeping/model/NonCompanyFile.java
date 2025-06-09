/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CurrentInputFile.java
 * CurrentInputFile
 */
package nonprofitbookkeeping.model;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Manages a statically accessible file reference, intended for files
 * that are not directly part of a "Company" data structure.
 * This might be used for importing external data or other auxiliary file operations.
 * Note: The use of a public static mutable field is generally discouraged as it can lead
 * to tight coupling and make state management difficult.
 */
public class NonCompanyFile
{

	/**
	 * The currently referenced non-company file.
	 * This is a public static field, allowing it to be accessed and modified globally.
	 * Marked with {@code @JsonProperty} though static fields are not typically serialized
	 * as part of an object instance by Jackson unless special configuration is applied.
	 */
	@JsonProperty public static File currentFile = null;

	/**
	 * Gets the currently referenced non-company file.
	 * @return The current {@link File} object, or {@code null} if no file is set.
	 */
	public static File getCurrentFile()
	{
		return currentFile;
	}

	/**
	 * Sets the non-company file reference.
	 * @param currentFileIn The {@link File} to set as the current non-company file.
	 *                      Can be {@code null} to clear the reference.
	 */
	public static void setCurrentFile(File currentFileIn)
	{
		currentFile = currentFileIn;
	}
	
}
