/**
 * 
 */
package nonprofitbookkeeping.core;

import nonprofitbookkeeping.api.DataStore;
import nonprofitbookkeeping.ui.helpers.ActionCancelledException;
import nonprofitbookkeeping.ui.helpers.NoFileCreatedException;
import nonprofitbookkeeping.ui.panels.AlertBox;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jackson-based implementation of the DataStore interface.
 * Uses Jackson's ObjectMapper to serialize and deserialize JSON,
 * including support for Java 8 date/time types.
 */
public class JacksonDataStore implements DataStore
{
	
	private ObjectMapper mapper;
	public static JacksonDataStore dataStore = new JacksonDataStore();
	
	/**
	 * Constructs a new JacksonDataStore with default settings:
	 * - Registers the JavaTimeModule for LocalDate/Instant support
	 * - Enables pretty-printing of JSON output
	 */
	public JacksonDataStore()
	{
		this.mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.enable(SerializationFeature.INDENT_OUTPUT)
			.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)			
			.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
	}
	
	/**
	 * Load 
	 * 
	 * Override @see nonprofitbookkeeping.api.DataStore#load(java.lang.Class, java.io.File)
	 * @throws ActionCancelledException 
	 * @throws NoFileCreatedException 
	 */
	@Override public <T> T load(Class<T> type, File file) throws IOException, ActionCancelledException, NoFileCreatedException
	{
		ObjectReader r = this.mapper.reader();
		
		T value = null;
		
		try
		{			
			value = r.readValue(file, type);
		}
		catch (StreamReadException e)
		{
			e.printStackTrace();
		}
		catch (DatabindException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return value;
	}
	
	/**
	 * save
	 * 
	 * Override @see nonprofitbookkeeping.api.DataStore#save(java.lang.Object, java.io.File)
	 * @throws ActionCancelledException 
	 * @throws NoFileCreatedException 
	 */
	@Override public void save(Object obj, File file) throws IOException, ActionCancelledException, NoFileCreatedException
	{
		
		if (file == null)
		{
			AlertBox.showError(null, "No open file");
			return;
		}		
		
		this.mapper.writeValue(file, obj);
		file = null;
		
	}
	
	/**
	 * listFiles
	 * 
	 * Override @see nonprofitbookkeeping.api.DataStore#listFiles(java.io.File, java.lang.String)
	 */
	@Override public List<File> listFiles(File directory, String extension)
	{
		
		if (directory == null || !directory.isDirectory())
		{
			return List.of();
		}
		
		return Arrays.stream(directory.listFiles((dir, name) -> name.endsWith(extension)))
			.collect(Collectors.toList());
	}

	/**
	 * @param dataStore the dataStore to set
	 */
	public static void setDataStore(JacksonDataStore dataStoreIn)
	{
		JacksonDataStore.dataStore = dataStoreIn;
	}

	/**
	 * @return the dataStore
	 */
	public static JacksonDataStore getDataStore()
	{
		return JacksonDataStore.dataStore;
	}
	
	
	
}
