package nonprofitbookkeeping.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Read-only projection for grant traceability/reporting rows exposed by
 * {@code v_grant_restriction_reporting}.
 */
public class GrantTraceabilityRow
{
	private String grantRecordId;
	private String grantId;
	private String grantReferenceNumber;
	private String status;
	private String complianceStatus;
	private String restrictionClass;
	private String fundCode;
	private String fundName;
	private String activityCode;
	private String activityName;
	private String donorOrContact;
	private BigDecimal awardedAmount;
	private BigDecimal recognizedAmount;
	private BigDecimal deferredAmount;
	private BigDecimal unrecognizedBalance;
	private LocalDate nextReportDue;

	public String getGrantRecordId() { return grantRecordId; }
	public void setGrantRecordId(String grantRecordId) { this.grantRecordId = grantRecordId; }
	public String getGrantId() { return grantId; }
	public void setGrantId(String grantId) { this.grantId = grantId; }
	public String getGrantReferenceNumber() { return grantReferenceNumber; }
	public void setGrantReferenceNumber(String grantReferenceNumber) { this.grantReferenceNumber = grantReferenceNumber; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getComplianceStatus() { return complianceStatus; }
	public void setComplianceStatus(String complianceStatus) { this.complianceStatus = complianceStatus; }
	public String getRestrictionClass() { return restrictionClass; }
	public void setRestrictionClass(String restrictionClass) { this.restrictionClass = restrictionClass; }
	public String getFundCode() { return fundCode; }
	public void setFundCode(String fundCode) { this.fundCode = fundCode; }
	public String getFundName() { return fundName; }
	public void setFundName(String fundName) { this.fundName = fundName; }
	public String getActivityCode() { return activityCode; }
	public void setActivityCode(String activityCode) { this.activityCode = activityCode; }
	public String getActivityName() { return activityName; }
	public void setActivityName(String activityName) { this.activityName = activityName; }
	public String getDonorOrContact() { return donorOrContact; }
	public void setDonorOrContact(String donorOrContact) { this.donorOrContact = donorOrContact; }
	public BigDecimal getAwardedAmount() { return awardedAmount; }
	public void setAwardedAmount(BigDecimal awardedAmount) { this.awardedAmount = awardedAmount; }
	public BigDecimal getRecognizedAmount() { return recognizedAmount; }
	public void setRecognizedAmount(BigDecimal recognizedAmount) { this.recognizedAmount = recognizedAmount; }
	public BigDecimal getDeferredAmount() { return deferredAmount; }
	public void setDeferredAmount(BigDecimal deferredAmount) { this.deferredAmount = deferredAmount; }
	public BigDecimal getUnrecognizedBalance() { return unrecognizedBalance; }
	public void setUnrecognizedBalance(BigDecimal unrecognizedBalance) { this.unrecognizedBalance = unrecognizedBalance; }
	public LocalDate getNextReportDue() { return nextReportDue; }
	public void setNextReportDue(LocalDate nextReportDue) { this.nextReportDue = nextReportDue; }
}
