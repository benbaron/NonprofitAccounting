
package nonprofitbookkeeping.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import nonprofitbookkeeping.ui.panels.GrantsPanel.Grant;
import nonprofitbookkeeping.ui.panels.GrantsPanel.GrantsService;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * FileBasedGrantsService retrieves grant data from a JSON file.
 * The JSON file should contain an array of grant objects.
 * In production, this would be replaced with an implementation that queries a database or external API.
 */
public class FileBasedGrantsService implements GrantsService
{
	
	/**
	 * 
	 */
	public class GrantsService
	{
		
	}
	
	private static final String DATA_FILE = System.getProperty("user.home") + File.separator +
		"NonprofitData" + File.separator + "grants.json";
	private final ObjectMapper mapper = new ObjectMapper();
	
	@Override public List<Grant> getAllGrants()
	{
		
		try
		{
			File file = new File(DATA_FILE);
			
			if (!file.exists())
			{
				return Collections.emptyList();
			}
			
			return this.mapper.readValue(file, new TypeReference<List<Grant>>()
			{
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Collections.emptyList();
		}
		
	}
	
}
