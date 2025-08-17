
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean representing the NEWSLETTER_15 report. Contains a list of advertising
 * rows as well as numerous singleton fields describing subscription metrics
 * and income figures for the newsletter.
 */
public class Newsletter15Bean extends ScaRowBase {
	private List<Newsletter15Row> rows = new ArrayList<>();
	
	private String newsletterName;
	private Integer issuesPerSubscription;
	private BigDecimal pricePerIssue;
	private BigDecimal pricePerSubscription;
	private Integer startSubscriptionsDue;
	private Integer endSubscriptionsDue;
	private Integer expiring;
	private Integer remaining;
	private BigDecimal subscriptionDue;
	private BigDecimal grossIncome;
	private BigDecimal adjustedGrossIncome;
	
	public List<Newsletter15Row> getRows()
	{
		return rows;
		
	}
	
	public void setRows(List<Newsletter15Row> rows)
	{
		this.rows = rows;
		
	}
	
	public String getNewsletterName()
	{
		return newsletterName;
		
	}
	
	public void setNewsletterName(String newsletterName)
	{
		this.newsletterName = newsletterName;
		
	}
	
	public Integer getIssuesPerSubscription()
	{
		return issuesPerSubscription;
		
	}
	
	public void setIssuesPerSubscription(Integer issuesPerSubscription)
	{
		this.issuesPerSubscription = issuesPerSubscription;
		
	}
	
	public BigDecimal getPricePerIssue()
	{
		return pricePerIssue;
		
	}
	
	public void setPricePerIssue(BigDecimal pricePerIssue)
	{
		this.pricePerIssue = pricePerIssue;
		
	}
	
	public BigDecimal getPricePerSubscription()
	{
		return pricePerSubscription;
		
	}
	
	public void setPricePerSubscription(BigDecimal pricePerSubscription)
	{
		this.pricePerSubscription = pricePerSubscription;
		
	}
	
	public Integer getStartSubscriptionsDue()
	{
		return startSubscriptionsDue;
		
	}
	
	public void setStartSubscriptionsDue(Integer startSubscriptionsDue)
	{
		this.startSubscriptionsDue = startSubscriptionsDue;
		
	}
	
	public Integer getEndSubscriptionsDue()
	{
		return endSubscriptionsDue;
		
	}
	
	public void setEndSubscriptionsDue(Integer endSubscriptionsDue)
	{
		this.endSubscriptionsDue = endSubscriptionsDue;
		
	}
	
	public Integer getExpiring()
	{
		return expiring;
		
	}
	
	public void setExpiring(Integer expiring)
	{
		this.expiring = expiring;
		
	}
	
	public Integer getRemaining()
	{
		return remaining;
		
	}
	
	public void setRemaining(Integer remaining)
	{
		this.remaining = remaining;
		
	}
	
	public BigDecimal getSubscriptionDue()
	{
		return subscriptionDue;
		
	}
	
	public void setSubscriptionDue(BigDecimal subscriptionDue)
	{
		this.subscriptionDue = subscriptionDue;
		
	}
	
	public BigDecimal getGrossIncome()
	{
		return grossIncome;
		
	}
	
	public void setGrossIncome(BigDecimal grossIncome)
	{
		this.grossIncome = grossIncome;
		
	}
	
	public BigDecimal getAdjustedGrossIncome()
	{
		return adjustedGrossIncome;
		
	}
	
	public void setAdjustedGrossIncome(BigDecimal adjustedGrossIncome)
	{
		this.adjustedGrossIncome = adjustedGrossIncome;
		
	}
	
}
