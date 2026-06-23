package org.nonprofitbookkeeping.service;

import org.nonprofitbookkeeping.persistence.Jpa;
import org.nonprofitbookkeeping.model.FundType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Queries fund balances by summing signed split amounts as of a date. */
@ApplicationScoped
public class FundBalanceService
{
    @Inject
    private Jpa jpa;

    public FundBalanceService()
    {
    }

    public FundBalanceService(Jpa jpa)
    {
        this.jpa = jpa;
    }

    public List<FundBalanceRow> balancesAsOf(LocalDate asOf)
    {
        try (EntityManager em = this.jpa.em())
        {
            List<Object[]> rows = em.createQuery(
                "select f.code, f.name, f.fundType, " +
                    "coalesce(sum(s.amountSigned), 0) " +
                    "from TxnSplit s " +
                    "join s.txn t " +
                    "join s.fund f " +
                    "where t.txnDate <= :asOf " +
                    "group by f.code, f.name, f.fundType " +
                    "order by f.code", Object[].class)
                .setParameter("asOf", asOf)
                .getResultList();

            List<FundBalanceRow> out = new ArrayList<>();
            for (Object[] row : rows)
            {
                out.add(new FundBalanceRow(
                    (String) row[0],
                    (String) row[1],
                    (FundType) row[2],
                    (BigDecimal) row[3]));
            }
            return out;
        }
    }
}
