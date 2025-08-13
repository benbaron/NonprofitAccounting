package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents the Sign-On Response (SONRS) aggregate in an OFX file.
 * This contains the status of the sign-on, server date and time, language,
 * and financial institution information.
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SignonResponse {
    /**
     * The status of the sign-on request.
     * Corresponds to the OFX aggregate {@code <STATUS>}.
     */
    @XmlElement(name = "STATUS")
    public Status status;

    /**
     * The date and time of the server processing the request.
     * Corresponds to the OFX tag {@code <DTSERVER>}.
     * Format is typically "YYYYMMDDHHMMSS[.mmm][Timezone]".
     */
    @XmlElement(name = "DTSERVER")
    public String dtServer;

    /**
     * The language used in the response.
     * Corresponds to the OFX tag {@code <LANGUAGE>}. (e.g., "ENG")
     */
    @XmlElement(name = "LANGUAGE")
    public String language;

    /**
     * Information about the financial institution.
     * Corresponds to the OFX aggregate {@code <FI>}.
     */
    @XmlElement(name = "FI")
    public FinancialInstitution fi;
}
