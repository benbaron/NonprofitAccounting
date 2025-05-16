package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class BankTransactionList 
{
    @XmlElement(name = "DTSTART")
    public String dtStart;

    @XmlElement(name = "DTEND")
    public String dtEnd;

    @XmlElement(name = "STMTTRN")
    public List<Transaction> stmtTrns;
}
