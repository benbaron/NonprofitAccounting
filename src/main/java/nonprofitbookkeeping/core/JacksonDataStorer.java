package nonprofitbookkeeping.core;

import nonprofitbookkeeping.api.DataStorer;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.ui.helpers.AlertBox;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.stream.Collectors;

public class JacksonDataStorer implements DataStorer
{
	private ObjectMapper mapper;
	public static JacksonDataStorer dataStorer = new JacksonDataStorer();
	private static final String JSON_ENTRY_NAME = "company_data.json";
	
	/**
	 * Constructs a JacksonDataStorer and initializes the Jackson ObjectMapper
	 * with specific configurations for serialization and deserialization.
	 */
	public JacksonDataStorer()
	{

                this.mapper = new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .enable(SerializationFeature.INDENT_OUTPUT)
                        .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
                this.mapper.configOverride(List.class)
                        .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
                this.mapper.getFactory().configure(
                        JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        }
	
	/**
	 * {@inheritDoc}
	 * This implementation reads data from a ZIP file, expecting a JSON entry named "company_data.json".
	 */
	@Override public <T> T loadData(Class<T> type, File file)	throws IOException,
																ActionCancelledException,
																NoFileCreatedException
	{
		System.out.println("DEBUG: Entering loadData"); // Minimal logging
		T value = null;
		
		try (FileInputStream fis = new FileInputStream(file);
			ZipInputStream zis = new ZipInputStream(fis))
		{
			ZipEntry zipEntry;
			boolean entryFound = false;
			
			while ((zipEntry = zis.getNextEntry()) != null)
			{
				
				if (JSON_ENTRY_NAME.equals(zipEntry.getName()))
				{
					value = this.mapper.readValue(zis, type);
					entryFound = true;
					break;
				}
				
				zis.closeEntry();
			}
			
			if (!entryFound)
			{
				// System.out.println("loadData: ERROR - company_data.json not found in zip");
				throw new IOException("Entry '" + JSON_ENTRY_NAME + "' not found in the zip file.");
			}
			
		}
		catch (StreamReadException e)
		{
			e.printStackTrace();
			throw new IOException("Error reading JSON stream from zip: " + e.getMessage(), e);
		}
		catch (DatabindException e)
		{
			e.printStackTrace();
			throw new IOException("Error deserializing JSON data from zip: " + e.getMessage(), e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw e;
		}
		
		System.out.println("DEBUG: Exiting loadData"); // Minimal logging
		return value;
	}
	
	/**
	 * {@inheritDoc}
	 * This implementation writes data to a ZIP file, storing it as a JSON entry named "company_data.json".
	 */
	@Override public void saveData(Object obj, File file)	throws IOException,
															ActionCancelledException,
															NoFileCreatedException
	{
		System.out.println("DEBUG: Entering saveData"); // Minimal logging
		
		if (file == null)
		{
			AlertBox.showError(null, "No open file");
			return;
		}
		
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileOutputStream fos = new FileOutputStream(file);
			ZipOutputStream zos = new ZipOutputStream(fos))
		{
			
			this.mapper.writeValue(baos, obj);
			
			ZipEntry zipEntry = new ZipEntry(JSON_ENTRY_NAME);
			zos.putNextEntry(zipEntry);
			zos.write(baos.toByteArray());
			zos.closeEntry();
			System.out.println("DEBUG: Exiting saveData"); // Minimal logging
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new IOException("Error saving data to zip file: " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override public List<File> listFiles(File directory, String extension)
	{
		
		if (directory == null || !directory.isDirectory())
		{
			return List.of();
		}
		
		List<File> result =
			Arrays.stream(directory.listFiles((dir, name) -> name.endsWith(extension)))
				.collect(Collectors.toList());
		return result;
	}
	
	/**
	 * Sets the static dataStorer instance.
	 * @param dataStorerIn The JacksonDataStorer instance to set.
	 */
	public static void setDataStorer(JacksonDataStorer dataStorerIn)
	{
		JacksonDataStorer.dataStorer = dataStorerIn;
	}
	
	/**
	 * Gets the static dataStorer instance.
	 * @return The static JacksonDataStorer instance.
	 */
	public static JacksonDataStorer getDataStorer()
	{
		return JacksonDataStorer.dataStorer;
	}
	

}
