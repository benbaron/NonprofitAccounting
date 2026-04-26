package nonprofitbookkeeping.persistence.sclx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.persistence.JsonStorageRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Shared JSON-storage repository for SCLX bean types.
 *
 * <p>Supports both a default singleton document per bean type and keyed documents
 * for multi-record use cases.</p>
 *
 * @param <T> bean type
 */
public abstract class AbstractSclxBeanRepository<T>
{
    private static final String DEFAULT_ID = "default";

    private final JsonStorageRepository jsonStorageRepository = new JsonStorageRepository();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final String storageKeyPrefix;
    private final Class<T> beanType;

    protected AbstractSclxBeanRepository(String storageKeyPrefix, Class<T> beanType)
    {
        this.storageKeyPrefix = storageKeyPrefix;
        this.beanType = beanType;
    }

    /**
     * Saves the default payload for this bean type.
     */
    public void save(T bean) throws SQLException
    {
        save(DEFAULT_ID, bean);
    }

    /**
     * Loads the default payload for this bean type.
     */
    public Optional<T> load() throws SQLException
    {
        return load(DEFAULT_ID);
    }

    /**
     * Deletes the default payload for this bean type.
     */
    public void delete() throws SQLException
    {
        delete(DEFAULT_ID);
    }

    /**
     * Saves a keyed payload for this bean type.
     */
    public void save(String id, T bean) throws SQLException
    {
        this.jsonStorageRepository.save(composeStorageKey(id), serialize(bean));
    }

    /**
     * Loads a keyed payload for this bean type.
     */
    public Optional<T> load(String id) throws SQLException
    {
        Optional<String> payload = this.jsonStorageRepository.load(composeStorageKey(id));
        if (payload.isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(deserialize(payload.get()));
    }

    /**
     * Deletes a keyed payload for this bean type.
     */
    public void delete(String id) throws SQLException
    {
        this.jsonStorageRepository.delete(composeStorageKey(id));
    }

    /**
     * Loads all keyed payloads (including the default id when present).
     */
    public Map<String, T> loadAll() throws SQLException
    {
        String sql = "SELECT storage_key, payload FROM json_storage WHERE storage_key LIKE ? ORDER BY storage_key";
        String prefix = this.storageKeyPrefix + "::";
        Map<String, T> rows = new LinkedHashMap<>();

        try (Connection connection = Database.get().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, prefix + "%");
            try (ResultSet rs = statement.executeQuery())
            {
                while (rs.next())
                {
                    String storageKey = rs.getString("storage_key");
                    String id = extractId(storageKey);
                    rows.put(id, deserialize(rs.getString("payload")));
                }
            }
        }

        return rows;
    }

    private String composeStorageKey(String id)
    {
        if (id == null || id.isBlank())
        {
            throw new IllegalArgumentException("id must not be null or blank");
        }
        return this.storageKeyPrefix + "::" + id;
    }

    private String extractId(String storageKey)
    {
        String prefix = this.storageKeyPrefix + "::";
        if (!storageKey.startsWith(prefix))
        {
            throw new IllegalStateException("Unexpected storage key for prefix " + this.storageKeyPrefix + ": " + storageKey);
        }
        return storageKey.substring(prefix.length());
    }

    private String serialize(T bean) throws SQLException
    {
        try
        {
            return this.objectMapper.writeValueAsString(bean);
        }
        catch (JsonProcessingException ex)
        {
            throw new SQLException("Unable to serialize SCLX bean for key prefix: " + this.storageKeyPrefix, ex);
        }
    }

    private T deserialize(String payload) throws SQLException
    {
        try
        {
            return this.objectMapper.readValue(payload, this.beanType);
        }
        catch (JsonProcessingException ex)
        {
            throw new SQLException("Unable to deserialize SCLX bean for key prefix: " + this.storageKeyPrefix, ex);
        }
    }
}
