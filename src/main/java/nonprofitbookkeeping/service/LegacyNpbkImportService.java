package nonprofitbookkeeping.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.persistence.CompanyDataRepository;
import nonprofitbookkeeping.persistence.CompanyRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service that imports legacy {@code .npbk} archives into the active H2 database.
 * <p>
 * Legacy archives are ZIP files that contain a {@code company_data.json} entry or
 * plain JSON files that serialize the {@link Company} aggregate. The importer
 * reads the archive, deserializes the company, and persists it using the modern
 * normalized tables as well as the blob store so that the UI can surface the new
 * company immediately.
 * </p>
 */
public class LegacyNpbkImportService
{
        private static final String LEGACY_ENTRY = "company_data.json";

        private final ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /**
         * Imports the supplied legacy archive into the currently active H2 database.
         *
         * @param archive path to a legacy {@code .npbk} ZIP file or plain JSON export
         * @return the identifier of the stored company record
         * @throws IOException  if the archive cannot be read or parsed
         * @throws SQLException if persisting the company fails
         */
        public long importArchive(Path archive) throws IOException, SQLException
        {
                if (archive == null)
                {
                        throw new IllegalArgumentException("archive cannot be null");
                }

                if (!Database.isInitialized())
                {
                        throw new IllegalStateException("Open or create an H2 database before importing legacy data.");
                }

                Company company = readCompany(archive);

                CompanyDataRepository dataRepository = new CompanyDataRepository();
                CompanyRepository companyRepository = new CompanyRepository();

                try
                {
                        dataRepository.persist(company);
                        return companyRepository.save(null, company);
                }
                catch (IOException | SQLException ex)
                {
                        throw ex;
                }
                catch (Exception ex)
                {
                        throw new IOException("Failed to import legacy archive", ex);
                }
        }

        private Company readCompany(Path archive) throws IOException
        {
                if (!Files.exists(archive))
                {
                        throw new IOException("Legacy archive not found: " + archive.toAbsolutePath());
                }

                if (looksLikeZip(archive))
                {
                        return readFromZip(archive);
                }

                return readFromJson(archive);
        }

        private boolean looksLikeZip(Path archive) throws IOException
        {
                try (InputStream in = Files.newInputStream(archive))
                {
                        byte[] signature = new byte[4];
                        int read = in.read(signature);
                        return read >= 2 && signature[0] == 'P' && signature[1] == 'K';
                }
        }

        private Company readFromZip(Path archive) throws IOException
        {
                try (InputStream in = Files.newInputStream(archive); ZipInputStream zin = new ZipInputStream(in))
                {
                        for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry())
                        {
                                if (LEGACY_ENTRY.equals(entry.getName()))
                                {
                                        return this.mapper.readValue(zin, Company.class);
                                }
                        }
                }

                throw new IOException(
                        "Entry '" + LEGACY_ENTRY + "' not found in archive " + archive.toAbsolutePath() + '.');
        }

        private Company readFromJson(Path archive) throws IOException
        {
                try (Reader reader = Files.newBufferedReader(archive, StandardCharsets.UTF_8))
                {
                        return this.mapper.readValue(reader, Company.class);
                }
        }
}
