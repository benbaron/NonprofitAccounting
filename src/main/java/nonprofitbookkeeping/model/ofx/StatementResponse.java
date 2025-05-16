package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class StatementResponse {
    @XmlElement(name = "CURDEF")
    public String curDef;

    @XmlElement(name = "BANKACCTFROM")
    public BankAccountInfo bankAcctFrom;

    @XmlElement(name = "BANKTRANLIST")
    public BankTransactionList bankTranList;

    @XmlElement(name = "LEDGERBAL")
    public Balance ledgerBal;
}
