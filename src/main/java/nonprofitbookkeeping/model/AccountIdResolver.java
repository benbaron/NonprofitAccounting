package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom ObjectIdResolver for Account that normalizes id keys to strings
 * so that numeric and string ids resolve to the same Account instance.
 */
public class AccountIdResolver implements ObjectIdResolver {
    private final Map<String, Object> idMap = new HashMap<>();

    @Override
    public void bindItem(ObjectIdGenerator.IdKey id, Object pojo) {
        if (id != null) {
            this.idMap.put(String.valueOf(id.key), pojo);
        }
    }

    @Override
    public Object resolveId(ObjectIdGenerator.IdKey id) {
        if (id == null) {
            return null;
        }
        return this.idMap.get(String.valueOf(id.key));
    }

    @Override
    public ObjectIdResolver newForDeserialization(Object context) {
        return new AccountIdResolver();
    }

    @Override
    public boolean canUseFor(ObjectIdResolver resolverType) {
        return resolverType.getClass() == this.getClass();
    }
}
