package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.GrantTraceabilityRow;
import nonprofitbookkeeping.persistence.GrantRecordRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service wrapper around traceability reporting view/repository queries.
 */
public class GrantTraceabilityService
{
	private final GrantRecordRepository repository;

	public GrantTraceabilityService()
	{
		this(new GrantRecordRepository());
	}

	public GrantTraceabilityService(GrantRecordRepository repository)
	{
		this.repository = repository;
	}

	public List<GrantTraceabilityRow> listTraceabilityRows() throws IOException
	{
		try
		{
			return this.repository.listTraceabilityRows();
		}
		catch (Exception ex)
		{
			throw new IOException("Failed to load grant traceability rows", ex);
		}
	}

	public List<GrantTraceabilityRow> listComplianceAlerts(LocalDate asOf) throws IOException
	{
		try
		{
			return this.repository.listComplianceAlerts(asOf);
		}
		catch (Exception ex)
		{
			throw new IOException("Failed to load grant compliance alerts", ex);
		}
	}
}
