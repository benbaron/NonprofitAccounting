package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents information about a financial institution as found in OFX data.
 * This typically includes the institution's organization name and its financial institution ID.
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FinancialInstitution {
    /**
     * The name of the organization (financial institution).
     * Corresponds to the OFX tag {@code <ORG>}.
     */
    @XmlElement(name = "ORG")
    public String org;

    /**
     * The Financial Institution ID. This is a unique identifier for the institution
     * within the OFX system (e.g., assigned by an OFX directory like Intuit's).
     * Corresponds to the OFX tag {@code <FID>}.
     */
    @XmlElement(name = "FID")
    public String fid;
}
