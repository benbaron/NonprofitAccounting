
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
 * This service uses Jackson for JSON processing.
 */
public final class ChartOfAccountsIOService
{
	
	/** Jackson ObjectMapper configured for ChartOfAccounts serialization and deserialization. */
	private final ObjectMapper mapper;
	
	/**
	 * Constructs a new {@code ChartOfAccountsIOService}.
	 * Initializes and configures a Jackson {@link ObjectMapper} with settings for
	 * handling Java Time (JSR-310) types, disabling timestamp writing for dates (uses ISO-8601 strings),
	 * and enabling indented (pretty-printed) JSON output.
	 */
	public ChartOfAccountsIOService()
	{
		this.mapper = new ObjectMapper()
			.registerModule(new JavaTimeModule()) // For Java 8 Date/Time types
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Use ISO-8601 strings
			.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
			// DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES is disabled by default, which is good for forward compatibility.
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Writes the given {@link ChartOfAccounts} object to a JSON file at the specified {@link Path}.
	 * If the parent directories for the path do not exist, they will be created.
	 * The output JSON will be pretty-printed.
	 *
	 * @param coa  The {@link ChartOfAccounts} object to persist. Must not be {@code null}.
	 * @param path The {@link Path} to the target JSON file. This can be an absolute or relative path.
	 *             The file will be overwritten if it already exists.
	 * @throws IOException if an error occurs during directory creation or file writing,
	 *                     or if {@code coa} cannot be serialized to JSON.
	 * @throws NullPointerException if {@code coa} or {@code path} is null.
	 */
	public void exportToJson(ChartOfAccounts coa, Path path) throws IOException
	{
		if (coa == null) {
            throw new NullPointerException("ChartOfAccounts object cannot be null.");
        }
        if (path == null) {
            throw new NullPointerException("Output path cannot be null.");
        }
		Files.createDirectories(path.getParent());
		this.mapper.writeValue(path.toFile(), coa);
	}
	
	/* ------------------------------------------------------------------ */
	/**
	 * Reads a {@link ChartOfAccounts} object from the JSON file at the specified {@link Path}.
	 * The JSON file is expected to be in the format previously produced by
	 * {@link #exportToJson(ChartOfAccounts, Path)}.
	 *
	 * @param path The {@link Path} to the JSON file to read.
	 * @return A fully populated, mutable {@link ChartOfAccounts} object.
	 * @throws IOException if the file does not exist, cannot be read, or contains invalid JSON
	 *                     that cannot be deserialized into a {@code ChartOfAccounts} object.
	 * @throws NullPointerException if {@code path} is null.
	 */
	public ChartOfAccounts importFromJson(Path path) throws IOException
	{
		if (path == null) {
            throw new NullPointerException("Input path cannot be null.");
        }
		return this.mapper.readValue(path.toFile(), ChartOfAccounts.class);
	}
	
}
