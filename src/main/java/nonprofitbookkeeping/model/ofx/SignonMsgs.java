package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents the Sign-On Messages (SIGNONMSGSRSV1) aggregate in an OFX response.
 * This typically contains a single sign-on response (SONRS).
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SignonMsgs {
    /**
     * The Sign-On Response (SONRS) which contains the status of the sign-on attempt
     * and information about the financial institution's server.
     * Corresponds to the OFX aggregate {@code <SONRS>}.
     */
    @XmlElement(name = "SONRS")
    public SignonResponse sonrs;
}
