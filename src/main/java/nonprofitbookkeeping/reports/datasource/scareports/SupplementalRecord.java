package nonprofitbookkeeping.reports.datasource.scareports;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Marker interface for supplemental report records that can be attached to a ledger entry.
 * Type information is retained during JSON serialization so concrete implementations can
 * be reconstructed during deserialization.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface SupplementalRecord {
}
