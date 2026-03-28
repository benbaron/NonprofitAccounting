
package nonprofitbookkeeping.model.sclx.generated;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;


/**
 * SCLX 1.3 - SCALedger Ledger Exchange Format
 * <p>
 * SCLX 1.3 supports both canonical balanced transactions and worksheet-native ledger entries, including single-sided transaction exports when the source workbook only exposes one posting side.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "format",
    "version",
    "exportedAt",
    "features",
    "compatibility",
    "organization",
    "reportingPeriod",
    "chartOfAccounts",
    "funds",
    "budgets",
    "people",
    "events",
    "documents",
    "transactions",
    "bankingItems",
    "outstandingItems",
    "otherAssetItems",
    "supplementalItems",
    "assets",
    "supplies",
    "bankStatementImports",
    "extensions"
})
@Generated("jsonschema2pojo")
public class SclxSchemaGenerated {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("format")
    @NotNull
    private String format;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    @NotNull
    private String version;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("exportedAt")
    @NotNull
    private Date exportedAt;
    @JsonProperty("features")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<@Valid String> features = new LinkedHashSet<String>();
    @JsonProperty("compatibility")
    @Valid
    private Compatibility compatibility;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organization")
    @Valid
    @NotNull
    private Organization organization;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportingPeriod")
    @Valid
    @NotNull
    private ReportingPeriod reportingPeriod;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("chartOfAccounts")
    @NotNull
    private List<@Valid Account> chartOfAccounts = new ArrayList<Account>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("funds")
    @NotNull
    private List<@Valid Fund> funds = new ArrayList<Fund>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgets")
    @NotNull
    private List<@Valid Budget> budgets = new ArrayList<Budget>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("people")
    @NotNull
    private List<@Valid Person> people = new ArrayList<Person>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("events")
    @NotNull
    private List<@Valid Event> events = new ArrayList<Event>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documents")
    @NotNull
    private List<@Valid Document> documents = new ArrayList<Document>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactions")
    @NotNull
    private List<@Valid Transaction> transactions = new ArrayList<Transaction>();
    @JsonProperty("bankingItems")
    private List<@Valid BankingItem> bankingItems = new ArrayList<BankingItem>();
    @JsonProperty("outstandingItems")
    private List<@Valid OutstandingItem> outstandingItems = new ArrayList<OutstandingItem>();
    @JsonProperty("otherAssetItems")
    private List<@Valid OtherAssetItem> otherAssetItems = new ArrayList<OtherAssetItem>();
    @JsonProperty("supplementalItems")
    private List<@Valid SupplementalItem> supplementalItems = new ArrayList<SupplementalItem>();
    @JsonProperty("assets")
    private List<@Valid Asset> assets = new ArrayList<Asset>();
    @JsonProperty("supplies")
    private List<@Valid Supply> supplies = new ArrayList<Supply>();
    @JsonProperty("bankStatementImports")
    private List<@Valid BankStatementImport> bankStatementImports = new ArrayList<BankStatementImport>();
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SclxSchemaGenerated() {
    }

    public SclxSchemaGenerated(String format, String version, Date exportedAt, Set<@Valid String> features, Compatibility compatibility, Organization organization, ReportingPeriod reportingPeriod, List<@Valid Account> chartOfAccounts, List<@Valid Fund> funds, List<@Valid Budget> budgets, List<@Valid Person> people, List<@Valid Event> events, List<@Valid Document> documents, List<@Valid Transaction> transactions, List<@Valid BankingItem> bankingItems, List<@Valid OutstandingItem> outstandingItems, List<@Valid OtherAssetItem> otherAssetItems, List<@Valid SupplementalItem> supplementalItems, List<@Valid Asset> assets, List<@Valid Supply> supplies, List<@Valid BankStatementImport> bankStatementImports, Extensions extensions) {
        super();
        this.format = format;
        this.version = version;
        this.exportedAt = exportedAt;
        this.features = features;
        this.compatibility = compatibility;
        this.organization = organization;
        this.reportingPeriod = reportingPeriod;
        this.chartOfAccounts = chartOfAccounts;
        this.funds = funds;
        this.budgets = budgets;
        this.people = people;
        this.events = events;
        this.documents = documents;
        this.transactions = transactions;
        this.bankingItems = bankingItems;
        this.outstandingItems = outstandingItems;
        this.otherAssetItems = otherAssetItems;
        this.supplementalItems = supplementalItems;
        this.assets = assets;
        this.supplies = supplies;
        this.bankStatementImports = bankStatementImports;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("format")
    public String getFormat() {
        return format;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("format")
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("exportedAt")
    public Date getExportedAt() {
        return exportedAt;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("exportedAt")
    public void setExportedAt(Date exportedAt) {
        this.exportedAt = exportedAt;
    }

    @JsonProperty("features")
    public Set<String> getFeatures() {
        return features;
    }

    @JsonProperty("features")
    public void setFeatures(Set<String> features) {
        this.features = features;
    }

    @JsonProperty("compatibility")
    public Compatibility getCompatibility() {
        return compatibility;
    }

    @JsonProperty("compatibility")
    public void setCompatibility(Compatibility compatibility) {
        this.compatibility = compatibility;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organization")
    public Organization getOrganization() {
        return organization;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("organization")
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportingPeriod")
    public ReportingPeriod getReportingPeriod() {
        return reportingPeriod;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("reportingPeriod")
    public void setReportingPeriod(ReportingPeriod reportingPeriod) {
        this.reportingPeriod = reportingPeriod;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("chartOfAccounts")
    public List<Account> getChartOfAccounts() {
        return chartOfAccounts;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("chartOfAccounts")
    public void setChartOfAccounts(List<Account> chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("funds")
    public List<Fund> getFunds() {
        return funds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("funds")
    public void setFunds(List<Fund> funds) {
        this.funds = funds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgets")
    public List<Budget> getBudgets() {
        return budgets;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("budgets")
    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("people")
    public List<Person> getPeople() {
        return people;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("people")
    public void setPeople(List<Person> people) {
        this.people = people;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("events")
    public List<Event> getEvents() {
        return events;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("events")
    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documents")
    public List<Document> getDocuments() {
        return documents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documents")
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactions")
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("transactions")
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @JsonProperty("bankingItems")
    public List<BankingItem> getBankingItems() {
        return bankingItems;
    }

    @JsonProperty("bankingItems")
    public void setBankingItems(List<BankingItem> bankingItems) {
        this.bankingItems = bankingItems;
    }

    @JsonProperty("outstandingItems")
    public List<OutstandingItem> getOutstandingItems() {
        return outstandingItems;
    }

    @JsonProperty("outstandingItems")
    public void setOutstandingItems(List<OutstandingItem> outstandingItems) {
        this.outstandingItems = outstandingItems;
    }

    @JsonProperty("otherAssetItems")
    public List<OtherAssetItem> getOtherAssetItems() {
        return otherAssetItems;
    }

    @JsonProperty("otherAssetItems")
    public void setOtherAssetItems(List<OtherAssetItem> otherAssetItems) {
        this.otherAssetItems = otherAssetItems;
    }

    @JsonProperty("supplementalItems")
    public List<SupplementalItem> getSupplementalItems() {
        return supplementalItems;
    }

    @JsonProperty("supplementalItems")
    public void setSupplementalItems(List<SupplementalItem> supplementalItems) {
        this.supplementalItems = supplementalItems;
    }

    @JsonProperty("assets")
    public List<Asset> getAssets() {
        return assets;
    }

    @JsonProperty("assets")
    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    @JsonProperty("supplies")
    public List<Supply> getSupplies() {
        return supplies;
    }

    @JsonProperty("supplies")
    public void setSupplies(List<Supply> supplies) {
        this.supplies = supplies;
    }

    @JsonProperty("bankStatementImports")
    public List<BankStatementImport> getBankStatementImports() {
        return bankStatementImports;
    }

    @JsonProperty("bankStatementImports")
    public void setBankStatementImports(List<BankStatementImport> bankStatementImports) {
        this.bankStatementImports = bankStatementImports;
    }

    @JsonProperty("extensions")
    public Extensions getExtensions() {
        return extensions;
    }

    @JsonProperty("extensions")
    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SclxSchemaGenerated.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("format");
        sb.append('=');
        sb.append(((this.format == null)?"<null>":this.format));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("exportedAt");
        sb.append('=');
        sb.append(((this.exportedAt == null)?"<null>":this.exportedAt));
        sb.append(',');
        sb.append("features");
        sb.append('=');
        sb.append(((this.features == null)?"<null>":this.features));
        sb.append(',');
        sb.append("compatibility");
        sb.append('=');
        sb.append(((this.compatibility == null)?"<null>":this.compatibility));
        sb.append(',');
        sb.append("organization");
        sb.append('=');
        sb.append(((this.organization == null)?"<null>":this.organization));
        sb.append(',');
        sb.append("reportingPeriod");
        sb.append('=');
        sb.append(((this.reportingPeriod == null)?"<null>":this.reportingPeriod));
        sb.append(',');
        sb.append("chartOfAccounts");
        sb.append('=');
        sb.append(((this.chartOfAccounts == null)?"<null>":this.chartOfAccounts));
        sb.append(',');
        sb.append("funds");
        sb.append('=');
        sb.append(((this.funds == null)?"<null>":this.funds));
        sb.append(',');
        sb.append("budgets");
        sb.append('=');
        sb.append(((this.budgets == null)?"<null>":this.budgets));
        sb.append(',');
        sb.append("people");
        sb.append('=');
        sb.append(((this.people == null)?"<null>":this.people));
        sb.append(',');
        sb.append("events");
        sb.append('=');
        sb.append(((this.events == null)?"<null>":this.events));
        sb.append(',');
        sb.append("documents");
        sb.append('=');
        sb.append(((this.documents == null)?"<null>":this.documents));
        sb.append(',');
        sb.append("transactions");
        sb.append('=');
        sb.append(((this.transactions == null)?"<null>":this.transactions));
        sb.append(',');
        sb.append("bankingItems");
        sb.append('=');
        sb.append(((this.bankingItems == null)?"<null>":this.bankingItems));
        sb.append(',');
        sb.append("outstandingItems");
        sb.append('=');
        sb.append(((this.outstandingItems == null)?"<null>":this.outstandingItems));
        sb.append(',');
        sb.append("otherAssetItems");
        sb.append('=');
        sb.append(((this.otherAssetItems == null)?"<null>":this.otherAssetItems));
        sb.append(',');
        sb.append("supplementalItems");
        sb.append('=');
        sb.append(((this.supplementalItems == null)?"<null>":this.supplementalItems));
        sb.append(',');
        sb.append("assets");
        sb.append('=');
        sb.append(((this.assets == null)?"<null>":this.assets));
        sb.append(',');
        sb.append("supplies");
        sb.append('=');
        sb.append(((this.supplies == null)?"<null>":this.supplies));
        sb.append(',');
        sb.append("bankStatementImports");
        sb.append('=');
        sb.append(((this.bankStatementImports == null)?"<null>":this.bankStatementImports));
        sb.append(',');
        sb.append("extensions");
        sb.append('=');
        sb.append(((this.extensions == null)?"<null>":this.extensions));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.bankingItems == null)? 0 :this.bankingItems.hashCode()));
        result = ((result* 31)+((this.reportingPeriod == null)? 0 :this.reportingPeriod.hashCode()));
        result = ((result* 31)+((this.documents == null)? 0 :this.documents.hashCode()));
        result = ((result* 31)+((this.chartOfAccounts == null)? 0 :this.chartOfAccounts.hashCode()));
        result = ((result* 31)+((this.format == null)? 0 :this.format.hashCode()));
        result = ((result* 31)+((this.transactions == null)? 0 :this.transactions.hashCode()));
        result = ((result* 31)+((this.bankStatementImports == null)? 0 :this.bankStatementImports.hashCode()));
        result = ((result* 31)+((this.version == null)? 0 :this.version.hashCode()));
        result = ((result* 31)+((this.people == null)? 0 :this.people.hashCode()));
        result = ((result* 31)+((this.features == null)? 0 :this.features.hashCode()));
        result = ((result* 31)+((this.extensions == null)? 0 :this.extensions.hashCode()));
        result = ((result* 31)+((this.assets == null)? 0 :this.assets.hashCode()));
        result = ((result* 31)+((this.budgets == null)? 0 :this.budgets.hashCode()));
        result = ((result* 31)+((this.supplementalItems == null)? 0 :this.supplementalItems.hashCode()));
        result = ((result* 31)+((this.supplies == null)? 0 :this.supplies.hashCode()));
        result = ((result* 31)+((this.organization == null)? 0 :this.organization.hashCode()));
        result = ((result* 31)+((this.otherAssetItems == null)? 0 :this.otherAssetItems.hashCode()));
        result = ((result* 31)+((this.exportedAt == null)? 0 :this.exportedAt.hashCode()));
        result = ((result* 31)+((this.funds == null)? 0 :this.funds.hashCode()));
        result = ((result* 31)+((this.compatibility == null)? 0 :this.compatibility.hashCode()));
        result = ((result* 31)+((this.events == null)? 0 :this.events.hashCode()));
        result = ((result* 31)+((this.outstandingItems == null)? 0 :this.outstandingItems.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SclxSchemaGenerated) == false) {
            return false;
        }
        SclxSchemaGenerated rhs = ((SclxSchemaGenerated) other);
        return (((((((((((((((((((((((this.bankingItems == rhs.bankingItems)||((this.bankingItems!= null)&&this.bankingItems.equals(rhs.bankingItems)))&&((this.reportingPeriod == rhs.reportingPeriod)||((this.reportingPeriod!= null)&&this.reportingPeriod.equals(rhs.reportingPeriod))))&&((this.documents == rhs.documents)||((this.documents!= null)&&this.documents.equals(rhs.documents))))&&((this.chartOfAccounts == rhs.chartOfAccounts)||((this.chartOfAccounts!= null)&&this.chartOfAccounts.equals(rhs.chartOfAccounts))))&&((this.format == rhs.format)||((this.format!= null)&&this.format.equals(rhs.format))))&&((this.transactions == rhs.transactions)||((this.transactions!= null)&&this.transactions.equals(rhs.transactions))))&&((this.bankStatementImports == rhs.bankStatementImports)||((this.bankStatementImports!= null)&&this.bankStatementImports.equals(rhs.bankStatementImports))))&&((this.version == rhs.version)||((this.version!= null)&&this.version.equals(rhs.version))))&&((this.people == rhs.people)||((this.people!= null)&&this.people.equals(rhs.people))))&&((this.features == rhs.features)||((this.features!= null)&&this.features.equals(rhs.features))))&&((this.extensions == rhs.extensions)||((this.extensions!= null)&&this.extensions.equals(rhs.extensions))))&&((this.assets == rhs.assets)||((this.assets!= null)&&this.assets.equals(rhs.assets))))&&((this.budgets == rhs.budgets)||((this.budgets!= null)&&this.budgets.equals(rhs.budgets))))&&((this.supplementalItems == rhs.supplementalItems)||((this.supplementalItems!= null)&&this.supplementalItems.equals(rhs.supplementalItems))))&&((this.supplies == rhs.supplies)||((this.supplies!= null)&&this.supplies.equals(rhs.supplies))))&&((this.organization == rhs.organization)||((this.organization!= null)&&this.organization.equals(rhs.organization))))&&((this.otherAssetItems == rhs.otherAssetItems)||((this.otherAssetItems!= null)&&this.otherAssetItems.equals(rhs.otherAssetItems))))&&((this.exportedAt == rhs.exportedAt)||((this.exportedAt!= null)&&this.exportedAt.equals(rhs.exportedAt))))&&((this.funds == rhs.funds)||((this.funds!= null)&&this.funds.equals(rhs.funds))))&&((this.compatibility == rhs.compatibility)||((this.compatibility!= null)&&this.compatibility.equals(rhs.compatibility))))&&((this.events == rhs.events)||((this.events!= null)&&this.events.equals(rhs.events))))&&((this.outstandingItems == rhs.outstandingItems)||((this.outstandingItems!= null)&&this.outstandingItems.equals(rhs.outstandingItems))));
    }

}
