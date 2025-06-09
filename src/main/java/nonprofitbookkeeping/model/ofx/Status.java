package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents the status of an OFX request or response.
 * This includes a status code, severity level, and an optional descriptive message.
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Status {
    /**
     * The status code indicating the result of an operation.
     * A code of 0 typically means success. Other codes indicate errors or warnings.
     * Corresponds to the OFX tag {@code <CODE>}.
     */
    @XmlElement(name = "CODE")
    public int code;

    /**
     * The severity of the status (e.g., "INFO", "WARN", "ERROR").
     * Corresponds to the OFX tag {@code <SEVERITY>}.
     */
    @XmlElement(name = "SEVERITY")
    public String severity;

    /**
     * An optional descriptive message providing more details about the status.
     * Corresponds to the OFX tag {@code <MESSAGE>}. Can be null or empty.
     */
    @XmlElement(name = "MESSAGE")
    public String message;
}
