package org.nonprofitbookkeeping.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;

class AlternateCompanyChoiceResolutionTest
{
    @Test
    void staleRecentLabelIsReplacedUsingStableCompanyId()
    {
        List<AlternateDataContextService.RecentCompanyChoice> normalized =
            AlternateDataContextService.normalizeRecentCompanies(
                List.of(new AlternateDataContextService.RecentCompanyChoice(
                    42L, "Old display text")),
                List.of(new CompanyRecord(42L, "Caer Galen",
                    Instant.parse("2026-01-01T00:00:00Z"))));

        assertEquals(1, normalized.size());
        assertEquals(42L, normalized.get(0).id());
        assertEquals("Caer Galen (ID: 42)", normalized.get(0).label());
    }

    @Test
    void recentCompanyMissingFromCurrentDatabaseIsRemoved()
    {
        List<AlternateDataContextService.RecentCompanyChoice> normalized =
            AlternateDataContextService.normalizeRecentCompanies(
                List.of(new AlternateDataContextService.RecentCompanyChoice(
                    99L, "Company from another database")),
                List.of(new CompanyRecord(42L, "Caer Galen",
                    Instant.parse("2026-01-01T00:00:00Z"))));

        assertEquals(List.of(), normalized);
    }
}
