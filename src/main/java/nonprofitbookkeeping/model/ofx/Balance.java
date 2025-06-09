package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

/**
 * Represents the balance of an account at a specific point in time,
 * as typically found in OFX financial data. This class is designed for
 * JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Balance {
    /**
     * The balance amount.
     * Corresponds to the OFX tag {@code <BALAMT>}.
     */
    @XmlElement(name = "BALAMT")
    public BigDecimal balAmt;

    /**
     * The date and time as of which the balance is reported.
     * Corresponds to the OFX tag {@code <DTASOF>}.
     * The format is typically a string like "YYYYMMDDHHMMSS[.mmm][Timezone]".
     */
    @XmlElement(name = "DTASOF")
    public String dtAsOf;
}
