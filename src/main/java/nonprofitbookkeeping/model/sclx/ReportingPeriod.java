
package nonprofitbookkeeping.model.sclx;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "startDate",
    "endDate",
    "label",
    "fiscalYear",
    "periodType",
    "extensions"
})
@Generated("jsonschema2pojo")
public class ReportingPeriod {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startDate")
    private String startDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endDate")
    private String endDate;
    @JsonProperty("label")
    private String label;
    @JsonProperty("fiscalYear")
    private Integer fiscalYear;
    @JsonProperty("periodType")
    private ReportingPeriod.PeriodType periodType;
    @JsonProperty("extensions")
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ReportingPeriod() {
    }

    public ReportingPeriod(String startDate, String endDate, String label, Integer fiscalYear, ReportingPeriod.PeriodType periodType, Extensions extensions) {
        super();
        this.startDate = startDate;
        this.endDate = endDate;
        this.label = label;
        this.fiscalYear = fiscalYear;
        this.periodType = periodType;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("startDate")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endDate")
    public String getEndDate() {
        return endDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("endDate")
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("fiscalYear")
    public Integer getFiscalYear() {
        return fiscalYear;
    }

    @JsonProperty("fiscalYear")
    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    @JsonProperty("periodType")
    public ReportingPeriod.PeriodType getPeriodType() {
        return periodType;
    }

    @JsonProperty("periodType")
    public void setPeriodType(ReportingPeriod.PeriodType periodType) {
        this.periodType = periodType;
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
        sb.append(ReportingPeriod.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("startDate");
        sb.append('=');
        sb.append(((this.startDate == null)?"<null>":this.startDate));
        sb.append(',');
        sb.append("endDate");
        sb.append('=');
        sb.append(((this.endDate == null)?"<null>":this.endDate));
        sb.append(',');
        sb.append("label");
        sb.append('=');
        sb.append(((this.label == null)?"<null>":this.label));
        sb.append(',');
        sb.append("fiscalYear");
        sb.append('=');
        sb.append(((this.fiscalYear == null)?"<null>":this.fiscalYear));
        sb.append(',');
        sb.append("periodType");
        sb.append('=');
        sb.append(((this.periodType == null)?"<null>":this.periodType));
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

    @Generated("jsonschema2pojo")
    public enum PeriodType {

        MONTH("MONTH"),
        QUARTER("QUARTER"),
        CALENDAR_YEAR("CALENDAR_YEAR"),
        FISCAL_YEAR("FISCAL_YEAR"),
        CUSTOM("CUSTOM");
        private final String value;
        private final static Map<String, ReportingPeriod.PeriodType> CONSTANTS = new HashMap<String, ReportingPeriod.PeriodType>();

        static {
            for (ReportingPeriod.PeriodType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        PeriodType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static ReportingPeriod.PeriodType fromValue(String value) {
            ReportingPeriod.PeriodType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
