package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Person;

/**
 * Repository for persisting {@link Person} payloads.
 */
@ApplicationScoped
public class PersonRepository extends AbstractSclxBeanRepository<Person>
{
    public PersonRepository()
    {
        super("sclx.Person", Person.class);
    }
}
