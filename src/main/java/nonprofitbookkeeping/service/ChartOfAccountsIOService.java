
package nonprofitbookkeeping.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import nonprofitbookkeeping.model.ChartOfAccounts;

/**
 * Utility for persisting a {@link ChartOfAccounts} to / from a human-readable
 * JSON document. The file format is:
 *
 * <pre>{@code
 * {
 *   "_schemaVersion" : 1,
 *   "rootAccounts"   : [ { ... }, { ... } ]
 * }
 * }</pre>
 *
 * <p>The mapper is pre-configured to:</p>
 * <ul>
 *   <li>write ISO-8601 strings for any {@code java.time} values,</li>
 *   <li>indent output for readability,</li>
 *   <li>ignore unknown JSON properties on import (forward compatibility).</li>
 * </ul>
 */
public final class ChartOfAccountsIOService
{
	
	private final ObjectMapper mapper;
	
	public ChartOfAccountsIOService()
	{
		mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.enable(SerializationFeature.INDENT_OUTPUT);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Writes {@code coa} to {@code path}, overwriting any existing file.
	 *
	 * @param coa  the chart to persist (must not be {@code null})
	 * @param path target file, absolute or relative
	 * @throws IOException if the file cannot be written
	 */
	public void exportToJson(ChartOfAccounts coa, Path path) throws IOException
	{
		Files.createDirectories(path.getParent());
		mapper.writeValue(path.toFile(), coa);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Reads a chart of accounts from the given JSON file.
	 *
	 * @param path JSON file previously produced by
	 *             {@link #exportToJson(ChartOfAccounts, Path)}
	 * @return a fully populated, mutable {@code ChartOfAccounts}
	 * @throws IOException if the file does not exist or contains invalid JSON
	 */
	public ChartOfAccounts importFromJson(Path path) throws IOException
	{
		return mapper.readValue(path.toFile(), ChartOfAccounts.class);
	}
	
}
