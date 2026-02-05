package nonprofitbookkeeping.model;

import java.math.BigDecimal;

public class UndepositedFundsItem
{
    private Long id;
    private String date_sent_received;
    private String date_transfer_or_check;
    private String date_on_statement;
    private String name_of_person_business;
    private String details_notes;
    private String from_to_card_merchant;
    private String account_for_payment_or_deposit;
    private BigDecimal amount;
    private String date_reversed;
    private String reversal_approved_by;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getDate_sent_received()
    {
        return date_sent_received;
    }

    public void setDate_sent_received(String date_sent_received)
    {
        this.date_sent_received = date_sent_received;
    }

    public String getDate_transfer_or_check()
    {
        return date_transfer_or_check;
    }

    public void setDate_transfer_or_check(String date_transfer_or_check)
    {
        this.date_transfer_or_check = date_transfer_or_check;
    }

    public String getDate_on_statement()
    {
        return date_on_statement;
    }

    public void setDate_on_statement(String date_on_statement)
    {
        this.date_on_statement = date_on_statement;
    }

    public String getName_of_person_business()
    {
        return name_of_person_business;
    }

    public void setName_of_person_business(String name_of_person_business)
    {
        this.name_of_person_business = name_of_person_business;
    }

    public String getDetails_notes()
    {
        return details_notes;
    }

    public void setDetails_notes(String details_notes)
    {
        this.details_notes = details_notes;
    }

    public String getFrom_to_card_merchant()
    {
        return from_to_card_merchant;
    }

    public void setFrom_to_card_merchant(String from_to_card_merchant)
    {
        this.from_to_card_merchant = from_to_card_merchant;
    }

    public String getAccount_for_payment_or_deposit()
    {
        return account_for_payment_or_deposit;
    }

    public void setAccount_for_payment_or_deposit(
        String account_for_payment_or_deposit)
    {
        this.account_for_payment_or_deposit =
            account_for_payment_or_deposit;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getDate_reversed()
    {
        return date_reversed;
    }

    public void setDate_reversed(String date_reversed)
    {
        this.date_reversed = date_reversed;
    }

    public String getReversal_approved_by()
    {
        return reversal_approved_by;
    }

    public void setReversal_approved_by(String reversal_approved_by)
    {
        this.reversal_approved_by = reversal_approved_by;
    }
}
