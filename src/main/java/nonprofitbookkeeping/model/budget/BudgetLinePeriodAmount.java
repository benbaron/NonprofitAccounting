package nonprofitbookkeeping.model.budget;

import java.math.BigDecimal;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

/**
 * Stores periodic amounts for a budget line when periodicity is not annual.
 */
@Entity
@Table(name = "budget_line_period_amount")
public class BudgetLinePeriodAmount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "line_id")
    private BudgetLine budgetLine;

    @Column(name = "period_index")
    private int periodIndex;

    private BigDecimal amount;

    public BudgetLinePeriodAmount() {}

    public BudgetLinePeriodAmount(BudgetLine budgetLine, int periodIndex, BigDecimal amount) {
        this.budgetLine = budgetLine;
        this.periodIndex = periodIndex;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BudgetLine getBudgetLine() {
        return budgetLine;
    }

    public void setBudgetLine(BudgetLine budgetLine) {
        this.budgetLine = budgetLine;
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(int periodIndex) {
        this.periodIndex = periodIndex;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
