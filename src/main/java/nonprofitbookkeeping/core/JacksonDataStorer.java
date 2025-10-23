
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

@Deprecated(forRemoval = true)
public class JacksonDataStorer implements DataStorer
{
        private static final Logger LOGGER = LoggerFactory.getLogger(JacksonDataStorer.class);

        private ObjectMapper mapper;
        public static JacksonDataStorer dataStorer = new JacksonDataStorer();
        private static final String JSON_ENTRY_NAME = "company_data.json";
        private static final String CHART_OF_ACCOUNTS_ENTRY_NAME = "chart_of_accounts.json";
        private static final String COMPANY_PROFILE_ENTRY_NAME = "company_profile.json";
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
         * This implementation reads data from a ZIP archive containing modular JSON sections.
         * Modern archives include {@code company_profile.json}, {@code ledger.json}, and
         * {@code chart_of_accounts.json} entries. The loader reassembles these sections into a
         * {@link Company} aggregate. For backwards compatibility, archives that still include a
         * monolithic {@code company_data.json} entry are also supported and will be merged with any
         * modular sections that appear alongside it.
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
                                        ChartOfAccounts chartOfAccounts = null;
                                        CompanyProfileModel profileModel = null;
                                        Ledger ledger = null;

                                        while ((zipEntry = zis.getNextEntry()) != null)
                                        {

                                                String entryName = zipEntry.getName();

                                                if (JSON_ENTRY_NAME.equals(entryName))
                                                {
                                                        value = this.mapper.readValue(zis, type);
                                                        entryFound = true;
                                                }
                                                else if (CHART_OF_ACCOUNTS_ENTRY_NAME.equals(entryName) &&
                                                        Company.class.isAssignableFrom(type))
                                                {
                                                        chartOfAccounts = this.mapper.readValue(zis,
                                                                ChartOfAccounts.class);
                                                }
                                                else if (COMPANY_PROFILE_ENTRY_NAME.equals(entryName) &&
                                                        Company.class.isAssignableFrom(type))
                                                {
                                                        profileModel = this.mapper.readValue(zis,
                                                                CompanyProfileModel.class);
                                                }
                                                else if (LEDGER_ENTRY_NAME.equals(entryName) &&
                                                        Company.class.isAssignableFrom(type))
                                                {
                                                        ledger = this.mapper.readValue(zis, Ledger.class);
                                                }

                                                zis.closeEntry();
                                        }

                                        if (value == null && Company.class.isAssignableFrom(type) &&
                                                (profileModel != null || ledger != null || chartOfAccounts != null))
                                        {
                                                Company company = new Company();

                                                if (profileModel != null)
                                                {
                                                        company.setCompanyProfileModel(profileModel);
                                                }

                                                if (ledger != null)
                                                {
                                                        company.setLedger(ledger);
                                                }

                                                if (chartOfAccounts != null)
                                                {
                                                        company.setChartOfAccounts(chartOfAccounts);
                                                }

                                                value = type.cast(company);
                                                entryFound = true;
                                        }
                                        else if (value instanceof Company company)
                                        {
                                                if (profileModel != null)
                                                {
                                                        company.setCompanyProfileModel(profileModel);
                                                }

                                                if (ledger != null)
                                                {
                                                        company.setLedger(ledger);
                                                }

                                                if (chartOfAccounts != null)
                                                {
                                                        company.setChartOfAccounts(chartOfAccounts);
                                                }
                                        }

                                        if (!entryFound)
                                        {
                                                throw new IOException(
                                                        "No company data entries found in the zip file.");
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
         * Serializes company aggregates into modular ZIP archives. Each major subsection is written to its
         * own JSON entry ({@code company_profile.json}, {@code ledger.json}, and
         * {@code chart_of_accounts.json}). A combined {@code company_data.json} payload is emitted as well so
         * legacy tooling can continue to consume the archive format.
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
                                writeEntry(zos, COMPANY_PROFILE_ENTRY_NAME,
                                        company.getCompanyProfileModel(), baos);
                                writeEntry(zos, LEDGER_ENTRY_NAME, company.getLedger(), baos);
                                writeEntry(zos, CHART_OF_ACCOUNTS_ENTRY_NAME,
                                        company.getChartOfAccounts(), baos);
                                writeEntry(zos, JSON_ENTRY_NAME, company, baos);
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

        private void writeEntry(ZipOutputStream zipStream,
                String entryName,
                Object content,
                ByteArrayOutputStream buffer) throws IOException
        {
                if (content == null)
                {
                        return;
                }

                buffer.reset();
                this.mapper.writeValue(buffer, content);
                ZipEntry entry = new ZipEntry(entryName);
                zipStream.putNextEntry(entry);
                zipStream.write(buffer.toByteArray());
                zipStream.closeEntry();
        }

}
