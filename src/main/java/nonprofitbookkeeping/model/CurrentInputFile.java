/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CurrentInputFile.java
 * CurrentInputFile
 */
package nonprofitbookkeeping.model;

import java.io.File;

/**
 * 
 */
public class CurrentInputFile
{

	public static File currentInputFile = null;

	/**
	 * @return the currentInputFile
	 */
	public static File getCurrentInputFile()
	{
		return currentInputFile;
	}

	/**
	 * @param currentInputFile the currentInputFile to set
	 */
	public static void setCurrentInputFile(File currentInputFileIn)
	{
		currentInputFile = currentInputFileIn;
	}
	
}
