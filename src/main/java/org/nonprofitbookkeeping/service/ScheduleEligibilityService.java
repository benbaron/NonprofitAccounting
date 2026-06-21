package org.nonprofitbookkeeping.service;

import org.nonprofitbookkeeping.model.*;
import org.nonprofitbookkeeping.persistence.Jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes which schedule kinds are applicable for a given account.
 *
 * Rules (new schema first, legacy fallback):
 * 1) Subtype defaults: account.subtype -> schedule kinds (via schedule_requirement_rule, subject_kind='SUBTYPE')
 * 2) Per-account overrides: schedule_requirement_rule rows for subject_kind='ACCOUNT' override subtype-level rows
 * 3) Legacy fallback remains available for un-migrated datasets
 *
 * The UI uses this to enable/disable schedule tabs (Excel-like behavior).
 */
@ApplicationScoped
public class ScheduleEligibilityService
{
    @Inject
    Jpa jpa;

    public ScheduleEligibilityService() {}

    public ScheduleEligibilityService(Jpa jpa)
    {
        this.jpa = jpa;
    }

    public Set<String> allowedScheduleKindCodes(Account account)
    {
        if (account == null) return Set.of();

        try (EntityManager em = this.jpa.em())
        {
            Set<String> normalized = normalizedScheduleCodes(em, account);
            if (!normalized.isEmpty())
            {
                return normalized;
            }
        }
        catch (RuntimeException ignore)
        {
            // If normalized schema is unavailable, legacy logic below keeps UI functional.
        }

        Set<String> legacy = new LinkedHashSet<>();
        if (account.getSubtype() != null)
        {
            legacy.addAll(defaultScheduleCodesForSubtype(account.getSubtype()));
        }
        legacy.addAll(accountRequirementScheduleCodes(account.getId()));
        return legacy;
    }

    public Set<String> defaultScheduleCodesForSubtype(AccountSubtype subtype)
    {
        if (subtype == null) return Set.of();

        // Legacy DB mapping (kept for compatibility).
        try (EntityManager em = this.jpa.em())
        {
            List<String> codes = em.createQuery(
                    "select k.code from AccountSubtypeScheduleDefault d join d.scheduleKind k where d.subtype = :s",
                    String.class)
                .setParameter("s", subtype.name())
                .getResultList();

            if (!codes.isEmpty())
            {
                return new LinkedHashSet<>(codes);
            }
        }
        catch (RuntimeException ignore)
        {
            // fall back below (useful during early dev)
        }

        // Fallback mapping (keeps UI usable even before seeding)
        return switch (subtype)
        {
            case RECEIVABLE -> Set.of("RECEIVABLE");
            case PAYABLE -> Set.of("PAYABLE");
            case PREPAID -> Set.of("PREPAID");
            case DEFERRED_REVENUE -> Set.of("DEFERRED_REVENUE");
            case INVENTORY -> Set.of("INVENTORY");
            case FIXED_ASSET -> Set.of("FIXED_ASSET");
            case OTHER_ASSET -> Set.of("OTHER_ASSET");
            case OTHER_LIABILITY -> Set.of("OTHER_LIABILITY");
            case CASH -> Set.of();
        };
    }

    private Set<String> accountRequirementScheduleCodes(Long accountId)
    {
        if (accountId == null) return Set.of();

        try (EntityManager em = this.jpa.em())
        {
            List<String> codes = em.createQuery(
                    "select k.code from AccountScheduleRequirement r join r.scheduleKind k where r.account.id = :aid",
                    String.class)
                .setParameter("aid", accountId)
                .getResultList();

            return new LinkedHashSet<>(codes);
        }
        catch (RuntimeException ex)
        {
            return Set.of();
        }
    }

    private Set<String> normalizedScheduleCodes(EntityManager em, Account account)
    {
        String subtype = account.getSubtype() == null ? null : account.getSubtype().name();
        Map<String, String> effective = new LinkedHashMap<>();

        if (subtype != null)
        {
            for (Object[] row : normalizedRulesForSubtype(em, subtype))
            {
                effective.put((String) row[0], (String) row[1]);
            }
        }

        if (account.getId() != null)
        {
            for (Object[] row : normalizedRulesForAccount(em, account.getId()))
            {
                // account-specific rule overrides subtype rule
                effective.put((String) row[0], (String) row[1]);
            }
        }

        return effective.entrySet().stream()
            .filter(e -> "REQUIRED".equals(e.getValue()) || "OPTIONAL".equals(e.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> normalizedRulesForSubtype(EntityManager em, String subtype)
    {
        Query q = em.createNativeQuery("""
            SELECT sk.code, srr.requirement_level
            FROM schedule_requirement_rule srr
            JOIN schedule_kind sk ON sk.id = srr.schedule_kind_id
            JOIN config_release cr ON cr.id = srr.config_release_id
            WHERE srr.subject_kind = 'SUBTYPE'
              AND srr.subtype = :subtype
              AND cr.status = 'ACTIVE'
              AND cr.effective_from <= CURRENT_DATE
              AND (cr.effective_to IS NULL OR cr.effective_to >= CURRENT_DATE)
              AND srr.valid_from <= CURRENT_DATE
              AND (srr.valid_to IS NULL OR srr.valid_to >= CURRENT_DATE)
            ORDER BY srr.precedence ASC, srr.id ASC
            """);
        q.setParameter("subtype", subtype);
        return q.getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> normalizedRulesForAccount(EntityManager em, Long accountId)
    {
        Query q = em.createNativeQuery("""
            SELECT sk.code, srr.requirement_level
            FROM schedule_requirement_rule srr
            JOIN schedule_kind sk ON sk.id = srr.schedule_kind_id
            JOIN config_release cr ON cr.id = srr.config_release_id
            WHERE srr.subject_kind = 'ACCOUNT'
              AND srr.account_id = :accountId
              AND cr.status = 'ACTIVE'
              AND cr.effective_from <= CURRENT_DATE
              AND (cr.effective_to IS NULL OR cr.effective_to >= CURRENT_DATE)
              AND srr.valid_from <= CURRENT_DATE
              AND (srr.valid_to IS NULL OR srr.valid_to >= CURRENT_DATE)
            ORDER BY srr.precedence ASC, srr.id ASC
            """);
        q.setParameter("accountId", accountId);
        return q.getResultList();
    }
}
