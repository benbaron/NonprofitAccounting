package org.nonprofitbookkeeping.model;

import jakarta.persistence.*;


@Entity
@Table(name = "fund_alias",
       indexes = {
           @Index(name = "ix_fund_alias_fund", columnList = "fund_id"),
           @Index(name = "ix_fund_alias_text", columnList = "alias_text")
       })
/**
 * Represents the FundAlias component in the nonprofit bookkeeping application.
 */
public class FundAlias
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(name = "alias_text", nullable = false, length = 400)
    private String aliasText;

    @Column(length = 80)
    private String source;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Long getId() { return this.id; }
    public Fund getFund() { return this.fund; }
    public void setFund(Fund fund) { this.fund = fund; }
    public String getAliasText() { return this.aliasText; }
    public void setAliasText(String aliasText) { this.aliasText = aliasText; }
    public String getSource() { return this.source; }
    public void setSource(String source) { this.source = source; }
    public boolean isActive() { return this.active; }
    public void setActive(boolean active) { this.active = active; }
}
