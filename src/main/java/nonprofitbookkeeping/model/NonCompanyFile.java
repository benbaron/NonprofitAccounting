/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CurrentInputFile.java
 * CurrentInputFile
 */
package nonprofitbookkeeping.model;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 */
public class NonCompanyFile
{

	@JsonProperty public static File currentFile = null;

	/**
	 * @return the currentInputFile
	 */
	public static File getCurrentFile()
	{
		return currentFile;
	}

	/**
	 * @param currentFile the currentInputFile to set
	 */
	public static void setCurrentFile(File currentFileIn)
	{
		currentFile = currentFileIn;
	}
	
}
