
package nonprofitbookkeeping.core;

import nonprofitbookkeeping.api.DataStorer;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.Ledger;
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
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class JacksonDataStorer.
 */
@Deprecated(forRemoval = true)
public class JacksonDataStorer implements DataStorer
{
        
        /** The Constant LOGGER. */
        private static final Logger LOGGER = LoggerFactory.getLogger(JacksonDataStorer.class);

        /** The mapper. */
        private ObjectMapper mapper;
        
        /** The data storer. */
        public static JacksonDataStorer dataStorer = new JacksonDataStorer();
        
        /** The Constant JSON_ENTRY_NAME. */
        private static final String JSON_ENTRY_NAME = "company_data.json";
        
        /** The Constant CHART_OF_ACCOUNTS_ENTRY_NAME. */
        private static final String CHART_OF_ACCOUNTS_ENTRY_NAME = "chart_of_accounts.json";
        
        /** The Constant COMPANY_PROFILE_ENTRY_NAME. */
        private static final String COMPANY_PROFILE_ENTRY_NAME = "company_profile.json";
        
        /** The Constant LEDGER_ENTRY_NAME. */
        private static final String LEDGER_ENTRY_NAME = "ledger.json";
	
	/**
	 * Constructs a JacksonDataStorer and initializes the Jackson ObjectMapper
	 * with specific configurations for serialization and deserialization.
	 */
	public JacksonDataStorer()
	{
		
		this.mapper = new ObjectMapper().registerModule(new JavaTimeModule())
			.enable(SerializationFeature.INDENT_OUTPUT)
			.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
		this.mapper.configOverride(List.class)
			.setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
		this.mapper.getFactory().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
	}
	
        /**
         * {@inheritDoc}
         * This implementation reads data from a ZIP file and reconstructs the requested type from
         * JSON entries. Legacy archives provide a {@code company_data.json} entry that serializes the
         * entire {@link Company}. Modern archives include modular entries for the chart of accounts,
         * company profile, and ledger ({@code chart_of_accounts.json}, {@code company_profile.json},
         * and {@code ledger.json} respectively). When loading a {@link Company}, those modular
         * sections are merged after deserialization. Requests for individual components (e.g.
         * {@link ChartOfAccounts}) are satisfied directly from their dedicated entries when present.
         */
	@Override public <T> T loadData(Class<T> type, File file)	throws IOException,
																ActionCancelledException,
																NoFileCreatedException
        {
                LOGGER.debug("Entering loadData");
                T value = null;
		
                try
                {
                        if (file == null)
                        {
                                throw new IOException("File is null");
                        }

                        if (isZipArchive(file))
                        {
                                try (FileInputStream fis = new FileInputStream(file);
                                        ZipInputStream zis = new ZipInputStream(fis))
                                {
                                        ZipEntry zipEntry;
                                        boolean entryFound = false;
                                        boolean componentFound = false;
                                        ChartOfAccounts chartOfAccounts = null;
                                        CompanyProfileModel profile = null;
                                        Ledger ledger = null;
                                        Company reconstructedCompany = null;

                                        while ((zipEntry = zis.getNextEntry()) != null)
                                        {

                                                String entryName = zipEntry.getName();

                                                if (JSON_ENTRY_NAME.equals(entryName))
                                                {
                                                        if (Company.class.isAssignableFrom(type))
                                                        {
                                                                value = this.mapper.readValue(zis, type);
                                                                entryFound = true;
                                                                if (value instanceof Company company)
                                                                {
                                                                        reconstructedCompany = company;
                                                                }
                                                        }
                                                        else
                                                        {
                                                                Company legacyCompany = this.mapper.readValue(zis,
                                                                        Company.class);

                                                                if (ChartOfAccounts.class.isAssignableFrom(type))
                                                                {
                                                                        if (value == null)
                                                                        {
                                                                                value = type.cast(
                                                                                        legacyCompany.getChartOfAccounts());
                                                                                entryFound = true;
                                                                        }
                                                                }
                                                                else if (CompanyProfileModel.class.isAssignableFrom(type))
                                                                {
                                                                        if (value == null)
                                                                        {
                                                                                value = type.cast(
                                                                                        legacyCompany.getCompanyProfileModel());
                                                                                entryFound = true;
                                                                        }
                                                                }
                                                                else if (Ledger.class.isAssignableFrom(type))
                                                                {
                                                                        if (value == null)
                                                                        {
                                                                                value = type.cast(legacyCompany.getLedger());
                                                                                entryFound = true;
                                                                        }
                                                                }
                                                        }
                                                }
                                                else if (CHART_OF_ACCOUNTS_ENTRY_NAME.equals(entryName))
                                                {
                                                        ChartOfAccounts chart = this.mapper.readValue(zis,
                                                                ChartOfAccounts.class);

                                                        if (Company.class.isAssignableFrom(type))
                                                        {
                                                                chartOfAccounts = chart;
                                                        }

                                                        if (ChartOfAccounts.class.isAssignableFrom(type))
                                                        {
                                                                value = type.cast(chart);
                                                                entryFound = true;
                                                        }

                                                        componentFound = true;
                                                }
                                                else if (COMPANY_PROFILE_ENTRY_NAME.equals(entryName))
                                                {
                                                        CompanyProfileModel profileModel = this.mapper.readValue(zis,
                                                                CompanyProfileModel.class);

                                                        if (Company.class.isAssignableFrom(type))
                                                        {
                                                                profile = profileModel;
                                                        }

                                                        if (CompanyProfileModel.class.isAssignableFrom(type))
                                                        {
                                                                value = type.cast(profileModel);
                                                                entryFound = true;
                                                        }

                                                        componentFound = true;
                                                }
                                                else if (LEDGER_ENTRY_NAME.equals(entryName))
                                                {
                                                        Ledger readLedger = this.mapper.readValue(zis, Ledger.class);

                                                        if (Company.class.isAssignableFrom(type))
                                                        {
                                                                ledger = readLedger;
                                                        }

                                                        if (Ledger.class.isAssignableFrom(type))
                                                        {
                                                                value = type.cast(readLedger);
                                                                entryFound = true;
                                                        }

                                                        componentFound = true;
                                                }
                                                zis.closeEntry();
                                        }

                                        if (Company.class.isAssignableFrom(type))
                                        {
                                                if (reconstructedCompany == null && (entryFound || componentFound))
                                                {
                                                        try
                                                        {
                                                                reconstructedCompany = (Company) type.getDeclaredConstructor()
                                                                        .newInstance();
                                                        }
                                                        catch (ReflectiveOperationException ex)
                                                        {
                                                                throw new IOException(
                                                                        "Failed to instantiate " + type.getName(), ex);
                                                        }
                                                }

                                                if (reconstructedCompany != null)
                                                {
                                                        if (profile != null)
                                                        {
                                                                reconstructedCompany.setCompanyProfileModel(profile);
                                                        }

                                                        if (ledger != null)
                                                        {
                                                                reconstructedCompany.setLedger(ledger);
                                                        }

                                                        if (chartOfAccounts != null)
                                                        {
                                                                reconstructedCompany.setChartOfAccounts(chartOfAccounts);
                                                        }

                                                        value = type.cast(reconstructedCompany);
                                                        entryFound = entryFound || componentFound;
                                                }
                                        }

                                        if (!entryFound)
                                        {
                                                throw new IOException(
                                                        "Entry '" + JSON_ENTRY_NAME + "' not found in the zip file.");
                                        }
                                }
                        }
                        else
                        {
                                try (InputStream is = new FileInputStream(file))
                                {
                                        value = this.mapper.readValue(is, type);
                                }
                        }
                }
                catch (StreamReadException e)
                {
                        LOGGER.error("Error reading JSON stream", e);
                        AlertBox.showError(null, "Error reading JSON stream: " + e.getMessage());
                        throw new IOException("Error reading JSON stream: " + e.getMessage(), e);
                }
                catch (DatabindException e)
                {
                        LOGGER.error("Error deserializing JSON data", e);
                        AlertBox.showError(null, "Error deserializing JSON data: " + e.getMessage());
                        throw new IOException("Error deserializing JSON data: " + e.getMessage(), e);
                }
                catch (IOException e)
                {
                        LOGGER.error("I/O error while loading data", e);
                        AlertBox.showError(null, "Error loading data: " + e.getMessage());
                        throw e;
                }

                LOGGER.debug("Exiting loadData");
                return value;
	}
	
        /**
         * {@inheritDoc}
         * This implementation writes data to a ZIP file. For general objects a single
         * {@code company_data.json} entry is produced. When persisting a {@link Company}, modular
         * entries for the profile, ledger, and chart of accounts are also written so that the archive
         * can be partially extracted without deserializing the entire aggregate. The legacy
         * {@code company_data.json} entry is still written to preserve backwards compatibility.
         */
	@Override public void saveData(Object obj, File file)	throws IOException,
															ActionCancelledException,
															NoFileCreatedException
        {
                LOGGER.debug("Entering saveData");

                if (file == null)
                {
                        AlertBox.showError(null, "No open file");
                        return;
                }

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        FileOutputStream fos = new FileOutputStream(file);
                        ZipOutputStream zos = new ZipOutputStream(fos))
                {

                        if (obj instanceof Company company)
                        {
                                writeEntry(zos, JSON_ENTRY_NAME, company, baos);
                                writeEntry(zos, COMPANY_PROFILE_ENTRY_NAME,
                                        company.getCompanyProfileModel(), baos);
                                writeEntry(zos, LEDGER_ENTRY_NAME, company.getLedger(), baos);
                                writeEntry(zos, CHART_OF_ACCOUNTS_ENTRY_NAME,
                                        company.getChartOfAccounts(), baos);
                        }
                        else
                        {
                                writeEntry(zos, JSON_ENTRY_NAME, obj, baos);
                        }
                        LOGGER.debug("Exiting saveData");
                }
                catch (IOException e)
                {
                        LOGGER.error("Error saving data to zip file", e);
                        AlertBox.showError(null, "Error saving data to zip file: " + e.getMessage());
                        throw new IOException("Error saving data to zip file: " + e.getMessage(), e);
                }

        }

        /**
         * Write entry.
         *
         * @param zos the zos
         * @param entryName the entry name
         * @param data the data
         * @param buffer the buffer
         * @throws IOException Signals that an I/O exception has occurred.
         */
        private void writeEntry(ZipOutputStream zos, String entryName, Object data,
                ByteArrayOutputStream buffer) throws IOException
        {
                if (data == null)
                {
                        return;
                }

                buffer.reset();
                this.mapper.writeValue(buffer, data);
                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                zos.write(buffer.toByteArray());
                zos.closeEntry();
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


        /**
         * Determines if the supplied file is a ZIP archive by inspecting its magic number.
         *
         * @param file the file to inspect
         * @return {@code true} if the file appears to be a ZIP archive, {@code false} otherwise
         * @throws IOException if the file cannot be read
         */
        private static boolean isZipArchive(File file) throws IOException
        {
                if (file == null || !file.exists())
                {
                        return false;
                }

                try (FileInputStream fis = new FileInputStream(file))
                {
                        byte[] signature = new byte[4];
                        int read = fis.read(signature);

                        if (read < 4)
                        {
                                return false;
                        }

                        return signature[0] == 0x50 && signature[1] == 0x4B &&
                                (signature[2] == 0x03 || signature[2] == 0x05 || signature[2] == 0x07) &&
                                (signature[3] == 0x04 || signature[3] == 0x06 || signature[3] == 0x08);
                }
        }


}
