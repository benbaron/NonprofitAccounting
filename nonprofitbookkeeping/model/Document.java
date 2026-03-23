
package nonprofitbookkeeping.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "documentId",
    "documentType",
    "referenceNumber",
    "documentDate",
    "fileName",
    "notes",
    "extensions"
})
@Generated("jsonschema2pojo")
public class Document {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentId")
    @Size(min = 1)
    @NotNull
    private String documentId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentType")
    @NotNull
    private Document.DocumentType documentType;
    @JsonProperty("referenceNumber")
    private String referenceNumber;
    @JsonProperty("documentDate")
    private String documentDate;
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("notes")
    private String notes;
    @JsonProperty("extensions")
    @Valid
    private Extensions extensions;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Document() {
    }

    public Document(String documentId, Document.DocumentType documentType, String referenceNumber, String documentDate, String fileName, String notes, Extensions extensions) {
        super();
        this.documentId = documentId;
        this.documentType = documentType;
        this.referenceNumber = referenceNumber;
        this.documentDate = documentDate;
        this.fileName = fileName;
        this.notes = notes;
        this.extensions = extensions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentId")
    public String getDocumentId() {
        return documentId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentId")
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentType")
    public Document.DocumentType getDocumentType() {
        return documentType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("documentType")
    public void setDocumentType(Document.DocumentType documentType) {
        this.documentType = documentType;
    }

    @JsonProperty("referenceNumber")
    public String getReferenceNumber() {
        return referenceNumber;
    }

    @JsonProperty("referenceNumber")
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    @JsonProperty("documentDate")
    public String getDocumentDate() {
        return documentDate;
    }

    @JsonProperty("documentDate")
    public void setDocumentDate(String documentDate) {
        this.documentDate = documentDate;
    }

    @JsonProperty("fileName")
    public String getFileName() {
        return fileName;
    }

    @JsonProperty("fileName")
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @JsonProperty("notes")
    public String getNotes() {
        return notes;
    }

    @JsonProperty("notes")
    public void setNotes(String notes) {
        this.notes = notes;
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
        sb.append(Document.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("documentId");
        sb.append('=');
        sb.append(((this.documentId == null)?"<null>":this.documentId));
        sb.append(',');
        sb.append("documentType");
        sb.append('=');
        sb.append(((this.documentType == null)?"<null>":this.documentType));
        sb.append(',');
        sb.append("referenceNumber");
        sb.append('=');
        sb.append(((this.referenceNumber == null)?"<null>":this.referenceNumber));
        sb.append(',');
        sb.append("documentDate");
        sb.append('=');
        sb.append(((this.documentDate == null)?"<null>":this.documentDate));
        sb.append(',');
        sb.append("fileName");
        sb.append('=');
        sb.append(((this.fileName == null)?"<null>":this.fileName));
        sb.append(',');
        sb.append("notes");
        sb.append('=');
        sb.append(((this.notes == null)?"<null>":this.notes));
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
    public enum DocumentType {

        RECEIPT("RECEIPT"),
        INVOICE("INVOICE"),
        CHECK_IMAGE("CHECK_IMAGE"),
        DEPOSIT_SLIP("DEPOSIT_SLIP"),
        BANK_STATEMENT("BANK_STATEMENT"),
        CONTRACT("CONTRACT"),
        APPROVAL_RECORD("APPROVAL_RECORD"),
        OTHER("OTHER");
        private final String value;
        private final static Map<String, Document.DocumentType> CONSTANTS = new HashMap<String, Document.DocumentType>();

        static {
            for (Document.DocumentType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        DocumentType(String value) {
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
        public static Document.DocumentType fromValue(String value) {
            Document.DocumentType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
