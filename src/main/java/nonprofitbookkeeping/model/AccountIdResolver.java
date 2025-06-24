package nonprofitbookkeeping.model;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.databind.deser.impl.SimpleObjectIdResolver;

/**
 * Custom resolver that stores Account object ids as strings so that
 * numeric and string references are treated equivalently.
 */
public class AccountIdResolver extends SimpleObjectIdResolver {
    @Override
    public void bindItem(ObjectIdGenerator.IdKey id, Object pojo) {
        super.bindItem(normalize(id), pojo);
    }

    @Override
    public Object resolveId(ObjectIdGenerator.IdKey id) {
        return super.resolveId(normalize(id));
    }

    private ObjectIdGenerator.IdKey normalize(ObjectIdGenerator.IdKey id) {
        if (id.key != null && !(id.key instanceof String)) {
            return new ObjectIdGenerator.IdKey(id.generator, id.scope, id.key.toString());
        }
        return id;
    }

    @Override
    public ObjectIdResolver newForDeserialization(Object context) {
        return new AccountIdResolver();
    }
}
