package nonprofitbookkeeping.persistence.sclx;

import jakarta.enterprise.context.ApplicationScoped;
import nonprofitbookkeeping.model.sclx.Event;

/**
 * Repository for persisting {@link Event} payloads.
 */
@ApplicationScoped
public class EventRepository extends AbstractSclxBeanRepository<Event>
{
    public EventRepository()
    {
        super("sclx.Event", Event.class);
    }
}
