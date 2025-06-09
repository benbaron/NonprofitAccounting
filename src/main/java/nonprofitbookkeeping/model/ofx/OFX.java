package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents the root element of an OFX (Open Financial Exchange) document.
 * This class encapsulates the main sections of an OFX file, typically including
 * sign-on messages and bank messages.
 * It is annotated for JAXB to facilitate XML marshalling and unmarshalling.
 */
@XmlRootElement(name = "OFX")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFX {
    /**
     * The sign-on messages response section.
     * This contains information related to the sign-on process, such as server status and user authentication details.
     * Corresponds to the OFX aggregate {@code <SIGNONMSGSRSV1>}.
     */
    @XmlElement(name = "SIGNONMSGSRSV1")
    public SignonMsgs signonMsgs;

    /**
     * The bank messages response section.
     * This contains financial data such as account statements and transaction lists.
     * Corresponds to the OFX aggregate {@code <BANKMSGSRSV1>}.
     */
    @XmlElement(name = "BANKMSGSRSV1")
    public BankMsgs bankMsgs;
}
