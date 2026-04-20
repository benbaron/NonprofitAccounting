package nonprofitbookkeeping.model;

import java.io.Serializable;

/**
 * Shared base model for simple record-like domain objects with an identifier and name.
 */
public abstract class AbstractRecordModel implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    protected AbstractRecordModel()
    {
        // default constructor for serialization frameworks
    }

    protected AbstractRecordModel(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean hasId()
    {
        return this.id != null && !this.id.isBlank();
    }

    public boolean hasName()
    {
        return this.name != null && !this.name.isBlank();
    }
}
