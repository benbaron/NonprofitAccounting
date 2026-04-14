package org.nonprofitbookkeeping.model;

import java.util.EnumSet;
import java.util.Map;

/**
 * Enumerates FundTransferStatus values used by the nonprofit bookkeeping application.
 */
public enum FundTransferStatus
{
    DRAFT,
    APPROVED,
    POSTING,
    POSTED,
    FAILED,
    VOIDED;

    private static final Map<FundTransferStatus, EnumSet<FundTransferStatus>> ALLOWED_NEXT =
        Map.of(
            DRAFT, EnumSet.of(APPROVED, VOIDED),
            APPROVED, EnumSet.of(POSTING, VOIDED),
            POSTING, EnumSet.of(POSTED, FAILED),
            FAILED, EnumSet.of(APPROVED, VOIDED),
            POSTED, EnumSet.noneOf(FundTransferStatus.class),
            VOIDED, EnumSet.noneOf(FundTransferStatus.class)
        );

    public static boolean isTransitionAllowed(FundTransferStatus from, FundTransferStatus to)
    {
        if (from == null || to == null)
        {
            return false;
        }
        if (from == to)
        {
            return true;
        }
        return ALLOWED_NEXT.getOrDefault(from, EnumSet.noneOf(FundTransferStatus.class)).contains(to);
    }
}
