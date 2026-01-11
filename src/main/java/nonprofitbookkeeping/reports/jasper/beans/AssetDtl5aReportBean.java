package nonprofitbookkeeping.reports.jasper.beans;

import java.util.List;

public class AssetDtl5aReportBean
{
    private java.lang.Double asset_dtl_5a_r2c3;
    private java.lang.Double contents_b59;
    private java.lang.Double contents_e_3;
    private java.lang.Double contents_e_4;
    private java.lang.Double amount_add_total_to_pg_3_i_a_end;
    private java.lang.Double prior_amount_total;
    private java.lang.Double current_amount_sum_f24_f34;
    private java.lang.Double prior_amount_17;
    private java.lang.Double current_amount_sum_f41_f45;
    private java.lang.Double prior_amount_26;
    private java.lang.Double current_amount_sum_f52_f59;
    private List<AssetDtl5aUndepositedFundsLineItem> undeposited_funds;
    private List<AssetDtl5aReceivableLineItem> receivables;
    private List<AssetDtl5aPrepaidExpenseLineItem> prepaid_expenses;
    private List<AssetDtl5aOtherAssetLineItem> other_assets;

    public java.lang.String getSending_branch_or_reason()
    {
        return getUndepositedLeftReason(0);
    }

    public java.lang.String getAmount()
    {
        return getUndepositedLeftAmount(0);
    }

    public java.lang.String getSending_branch_or_reason_2()
    {
        return getUndepositedRightReason(0);
    }

    public java.lang.String getAsset_dtl_5a_r15c6()
    {
        return getUndepositedRightDetail(0);
    }

    public java.lang.String getAmount_2()
    {
        return getUndepositedRightAmount(0);
    }

    public java.lang.String getSending_branch_or_reason_3()
    {
        return getUndepositedLeftReason(1);
    }

    public java.lang.String getAmount_3()
    {
        return getUndepositedLeftAmount(1);
    }

    public java.lang.String getSending_branch_or_reason_4()
    {
        return getUndepositedRightReason(1);
    }

    public java.lang.String getAsset_dtl_5a_r16c6()
    {
        return getUndepositedRightDetail(1);
    }

    public java.lang.String getAmount_4()
    {
        return getUndepositedRightAmount(1);
    }

    public java.lang.String getSending_branch_or_reason_5()
    {
        return getUndepositedLeftReason(2);
    }

    public java.lang.String getAmount_5()
    {
        return getUndepositedLeftAmount(2);
    }

    public java.lang.String getSending_branch_or_reason_6()
    {
        return getUndepositedRightReason(2);
    }

    public java.lang.String getAsset_dtl_5a_r17c6()
    {
        return getUndepositedRightDetail(2);
    }

    public java.lang.String getAmount_6()
    {
        return getUndepositedRightAmount(2);
    }

    public java.lang.String getSending_branch_or_reason_7()
    {
        return getUndepositedLeftReason(3);
    }

    public java.lang.String getAmount_7()
    {
        return getUndepositedLeftAmount(3);
    }

    public java.lang.String getSending_branch_or_reason_8()
    {
        return getUndepositedRightReason(3);
    }

    public java.lang.String getAsset_dtl_5a_r18c6()
    {
        return getUndepositedRightDetail(3);
    }

    public java.lang.String getAmount_8()
    {
        return getUndepositedRightAmount(3);
    }

    public java.lang.String getReceivables_owed_from()
    {
        return getReceivableValue(0, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason()
    {
        return getReceivableValue(0, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_9()
    {
        return getReceivableValue(0, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount()
    {
        return getReceivableValue(0, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount()
    {
        return getReceivableValue(0, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_2()
    {
        return getReceivableValue(1, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_2()
    {
        return getReceivableValue(1, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_10()
    {
        return getReceivableValue(1, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_2()
    {
        return getReceivableValue(1, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_2()
    {
        return getReceivableValue(1, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_3()
    {
        return getReceivableValue(2, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_3()
    {
        return getReceivableValue(2, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_11()
    {
        return getReceivableValue(2, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_3()
    {
        return getReceivableValue(2, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_3()
    {
        return getReceivableValue(2, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_4()
    {
        return getReceivableValue(3, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_4()
    {
        return getReceivableValue(3, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_12()
    {
        return getReceivableValue(3, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_4()
    {
        return getReceivableValue(3, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_4()
    {
        return getReceivableValue(3, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_5()
    {
        return getReceivableValue(4, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_5()
    {
        return getReceivableValue(4, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_13()
    {
        return getReceivableValue(4, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_5()
    {
        return getReceivableValue(4, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_5()
    {
        return getReceivableValue(4, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_6()
    {
        return getReceivableValue(5, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_6()
    {
        return getReceivableValue(5, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_14()
    {
        return getReceivableValue(5, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_6()
    {
        return getReceivableValue(5, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_6()
    {
        return getReceivableValue(5, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_7()
    {
        return getReceivableValue(6, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_7()
    {
        return getReceivableValue(6, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_15()
    {
        return getReceivableValue(6, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_7()
    {
        return getReceivableValue(6, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_7()
    {
        return getReceivableValue(6, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_8()
    {
        return getReceivableValue(7, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_8()
    {
        return getReceivableValue(7, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_16()
    {
        return getReceivableValue(7, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_8()
    {
        return getReceivableValue(7, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_8()
    {
        return getReceivableValue(7, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_9()
    {
        return getReceivableValue(8, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_9()
    {
        return getReceivableValue(8, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_17()
    {
        return getReceivableValue(8, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_9()
    {
        return getReceivableValue(8, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_9()
    {
        return getReceivableValue(8, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_10()
    {
        return getReceivableValue(9, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_10()
    {
        return getReceivableValue(9, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_18()
    {
        return getReceivableValue(9, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_10()
    {
        return getReceivableValue(9, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_10()
    {
        return getReceivableValue(9, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getReceivables_owed_from_11()
    {
        return getReceivableValue(10, AssetDtl5aReceivableLineItem::getReceivables_owed_from);
    }

    public java.lang.String getReason_11()
    {
        return getReceivableValue(10, AssetDtl5aReceivableLineItem::getReason);
    }

    public java.lang.String getSending_branch_or_reason_19()
    {
        return getReceivableValue(10, AssetDtl5aReceivableLineItem::getSending_branch_or_reason);
    }

    public java.lang.String getPrior_amount_11()
    {
        return getReceivableValue(10, AssetDtl5aReceivableLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_11()
    {
        return getReceivableValue(10, AssetDtl5aReceivableLineItem::getCurrent_amount);
    }

    public java.lang.String getPrepaid_expenses_description()
    {
        return getPrepaidExpenseValue(0, AssetDtl5aPrepaidExpenseLineItem::getPrepaid_expenses_description);
    }

    public java.lang.String getPrior_amount_12()
    {
        return getPrepaidExpenseValue(0, AssetDtl5aPrepaidExpenseLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_12()
    {
        return getPrepaidExpenseValue(0, AssetDtl5aPrepaidExpenseLineItem::getCurrent_amount);
    }

    public java.lang.String getPrepaid_expenses_description_2()
    {
        return getPrepaidExpenseValue(1, AssetDtl5aPrepaidExpenseLineItem::getPrepaid_expenses_description);
    }

    public java.lang.String getPrior_amount_13()
    {
        return getPrepaidExpenseValue(1, AssetDtl5aPrepaidExpenseLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_13()
    {
        return getPrepaidExpenseValue(1, AssetDtl5aPrepaidExpenseLineItem::getCurrent_amount);
    }

    public java.lang.String getPrepaid_expenses_description_3()
    {
        return getPrepaidExpenseValue(2, AssetDtl5aPrepaidExpenseLineItem::getPrepaid_expenses_description);
    }

    public java.lang.String getPrior_amount_14()
    {
        return getPrepaidExpenseValue(2, AssetDtl5aPrepaidExpenseLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_14()
    {
        return getPrepaidExpenseValue(2, AssetDtl5aPrepaidExpenseLineItem::getCurrent_amount);
    }

    public java.lang.String getPrepaid_expenses_description_4()
    {
        return getPrepaidExpenseValue(3, AssetDtl5aPrepaidExpenseLineItem::getPrepaid_expenses_description);
    }

    public java.lang.String getPrior_amount_15()
    {
        return getPrepaidExpenseValue(3, AssetDtl5aPrepaidExpenseLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_15()
    {
        return getPrepaidExpenseValue(3, AssetDtl5aPrepaidExpenseLineItem::getCurrent_amount);
    }

    public java.lang.String getPrepaid_expenses_description_5()
    {
        return getPrepaidExpenseValue(4, AssetDtl5aPrepaidExpenseLineItem::getPrepaid_expenses_description);
    }

    public java.lang.String getPrior_amount_16()
    {
        return getPrepaidExpenseValue(4, AssetDtl5aPrepaidExpenseLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_16()
    {
        return getPrepaidExpenseValue(4, AssetDtl5aPrepaidExpenseLineItem::getCurrent_amount);
    }

    public java.lang.String getOther_assets_description()
    {
        return getOtherAssetValue(0, AssetDtl5aOtherAssetLineItem::getOther_assets_description);
    }

    public java.lang.String getReason_12()
    {
        return getOtherAssetValue(0, AssetDtl5aOtherAssetLineItem::getReason);
    }

    public java.lang.String getShow_on()
    {
        return getOtherAssetValue(0, AssetDtl5aOtherAssetLineItem::getShow_on);
    }

    public java.lang.String getPrior_amount_18()
    {
        return getOtherAssetValue(0, AssetDtl5aOtherAssetLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_17()
    {
        return getOtherAssetValue(0, AssetDtl5aOtherAssetLineItem::getCurrent_amount);
    }

    public java.lang.String getOther_assets_description_2()
    {
        return getOtherAssetValue(1, AssetDtl5aOtherAssetLineItem::getOther_assets_description);
    }

    public java.lang.String getReason_13()
    {
        return getOtherAssetValue(1, AssetDtl5aOtherAssetLineItem::getReason);
    }

    public java.lang.String getShow_on_2()
    {
        return getOtherAssetValue(1, AssetDtl5aOtherAssetLineItem::getShow_on);
    }

    public java.lang.String getPrior_amount_19()
    {
        return getOtherAssetValue(1, AssetDtl5aOtherAssetLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_18()
    {
        return getOtherAssetValue(1, AssetDtl5aOtherAssetLineItem::getCurrent_amount);
    }

    public java.lang.String getOther_assets_description_3()
    {
        return getOtherAssetValue(2, AssetDtl5aOtherAssetLineItem::getOther_assets_description);
    }

    public java.lang.String getReason_14()
    {
        return getOtherAssetValue(2, AssetDtl5aOtherAssetLineItem::getReason);
    }

    public java.lang.String getShow_on_3()
    {
        return getOtherAssetValue(2, AssetDtl5aOtherAssetLineItem::getShow_on);
    }

    public java.lang.String getPrior_amount_20()
    {
        return getOtherAssetValue(2, AssetDtl5aOtherAssetLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_19()
    {
        return getOtherAssetValue(2, AssetDtl5aOtherAssetLineItem::getCurrent_amount);
    }

    public java.lang.String getOther_assets_description_4()
    {
        return getOtherAssetValue(3, AssetDtl5aOtherAssetLineItem::getOther_assets_description);
    }

    public java.lang.String getReason_15()
    {
        return getOtherAssetValue(3, AssetDtl5aOtherAssetLineItem::getReason);
    }

    public java.lang.String getShow_on_4()
    {
        return getOtherAssetValue(3, AssetDtl5aOtherAssetLineItem::getShow_on);
    }

    public java.lang.String getPrior_amount_21()
    {
        return getOtherAssetValue(3, AssetDtl5aOtherAssetLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_20()
    {
        return getOtherAssetValue(3, AssetDtl5aOtherAssetLineItem::getCurrent_amount);
    }

    public java.lang.String getOther_assets_description_5()
    {
        return getOtherAssetValue(4, AssetDtl5aOtherAssetLineItem::getOther_assets_description);
    }

    public java.lang.String getReason_16()
    {
        return getOtherAssetValue(4, AssetDtl5aOtherAssetLineItem::getReason);
    }

    public java.lang.String getShow_on_5()
    {
        return getOtherAssetValue(4, AssetDtl5aOtherAssetLineItem::getShow_on);
    }

    public java.lang.String getPrior_amount_22()
    {
        return getOtherAssetValue(4, AssetDtl5aOtherAssetLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_21()
    {
        return getOtherAssetValue(4, AssetDtl5aOtherAssetLineItem::getCurrent_amount);
    }

    public java.lang.String getOther_assets_description_6()
    {
        return getOtherAssetValue(5, AssetDtl5aOtherAssetLineItem::getOther_assets_description);
    }

    public java.lang.String getReason_17()
    {
        return getOtherAssetValue(5, AssetDtl5aOtherAssetLineItem::getReason);
    }

    public java.lang.String getShow_on_6()
    {
        return getOtherAssetValue(5, AssetDtl5aOtherAssetLineItem::getShow_on);
    }

    public java.lang.String getPrior_amount_23()
    {
        return getOtherAssetValue(5, AssetDtl5aOtherAssetLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_22()
    {
        return getOtherAssetValue(5, AssetDtl5aOtherAssetLineItem::getCurrent_amount);
    }

    public java.lang.String getOther_assets_description_7()
    {
        return getOtherAssetValue(6, AssetDtl5aOtherAssetLineItem::getOther_assets_description);
    }

    public java.lang.String getReason_18()
    {
        return getOtherAssetValue(6, AssetDtl5aOtherAssetLineItem::getReason);
    }

    public java.lang.String getShow_on_7()
    {
        return getOtherAssetValue(6, AssetDtl5aOtherAssetLineItem::getShow_on);
    }

    public java.lang.String getPrior_amount_24()
    {
        return getOtherAssetValue(6, AssetDtl5aOtherAssetLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_23()
    {
        return getOtherAssetValue(6, AssetDtl5aOtherAssetLineItem::getCurrent_amount);
    }

    public java.lang.String getOther_assets_description_8()
    {
        return getOtherAssetValue(7, AssetDtl5aOtherAssetLineItem::getOther_assets_description);
    }

    public java.lang.String getReason_19()
    {
        return getOtherAssetValue(7, AssetDtl5aOtherAssetLineItem::getReason);
    }

    public java.lang.String getShow_on_8()
    {
        return getOtherAssetValue(7, AssetDtl5aOtherAssetLineItem::getShow_on);
    }

    public java.lang.String getPrior_amount_25()
    {
        return getOtherAssetValue(7, AssetDtl5aOtherAssetLineItem::getPrior_amount);
    }

    public java.lang.String getCurrent_amount_24()
    {
        return getOtherAssetValue(7, AssetDtl5aOtherAssetLineItem::getCurrent_amount);
    }

    public java.lang.Double getAsset_dtl_5a_r2c3()
    {
        return asset_dtl_5a_r2c3;
    }

    public void setAsset_dtl_5a_r2c3(java.lang.Double v)
    {
        this.asset_dtl_5a_r2c3 = v;
    }

    public java.lang.Double getContents_b59()
    {
        return contents_b59;
    }

    public void setContents_b59(java.lang.Double v)
    {
        this.contents_b59 = v;
    }

    public java.lang.Double getContents_e_3()
    {
        return contents_e_3;
    }

    public void setContents_e_3(java.lang.Double v)
    {
        this.contents_e_3 = v;
    }

    public java.lang.Double getContents_e_4()
    {
        return contents_e_4;
    }

    public void setContents_e_4(java.lang.Double v)
    {
        this.contents_e_4 = v;
    }

    public java.lang.Double getAmount_add_total_to_pg_3_i_a_end()
    {
        return amount_add_total_to_pg_3_i_a_end;
    }

    public void setAmount_add_total_to_pg_3_i_a_end(java.lang.Double v)
    {
        this.amount_add_total_to_pg_3_i_a_end = v;
    }

    public java.lang.Double getPrior_amount_total()
    {
        return prior_amount_total;
    }

    public void setPrior_amount_total(java.lang.Double v)
    {
        this.prior_amount_total = v;
    }

    public java.lang.Double getCurrent_amount_sum_f24_f34()
    {
        return current_amount_sum_f24_f34;
    }

    public void setCurrent_amount_sum_f24_f34(java.lang.Double v)
    {
        this.current_amount_sum_f24_f34 = v;
    }

    public java.lang.Double getPrior_amount_17()
    {
        return prior_amount_17;
    }

    public void setPrior_amount_17(java.lang.Double v)
    {
        this.prior_amount_17 = v;
    }

    public java.lang.Double getCurrent_amount_sum_f41_f45()
    {
        return current_amount_sum_f41_f45;
    }

    public void setCurrent_amount_sum_f41_f45(java.lang.Double v)
    {
        this.current_amount_sum_f41_f45 = v;
    }

    public java.lang.Double getPrior_amount_26()
    {
        return prior_amount_26;
    }

    public void setPrior_amount_26(java.lang.Double v)
    {
        this.prior_amount_26 = v;
    }

    public java.lang.Double getCurrent_amount_sum_f52_f59()
    {
        return current_amount_sum_f52_f59;
    }

    public void setCurrent_amount_sum_f52_f59(java.lang.Double v)
    {
        this.current_amount_sum_f52_f59 = v;
    }

    public List<AssetDtl5aUndepositedFundsLineItem> getUndeposited_funds()
    {
        return undeposited_funds;
    }

    public void setUndeposited_funds(
        List<AssetDtl5aUndepositedFundsLineItem> v
    )
    {
        this.undeposited_funds = v;
    }

    public List<AssetDtl5aReceivableLineItem> getReceivables()
    {
        return receivables;
    }

    public void setReceivables(List<AssetDtl5aReceivableLineItem> v)
    {
        this.receivables = v;
    }

    public List<AssetDtl5aPrepaidExpenseLineItem> getPrepaid_expenses()
    {
        return prepaid_expenses;
    }

    public void setPrepaid_expenses(
        List<AssetDtl5aPrepaidExpenseLineItem> v
    )
    {
        this.prepaid_expenses = v;
    }

    public List<AssetDtl5aOtherAssetLineItem> getOther_assets()
    {
        return other_assets;
    }

    public void setOther_assets(List<AssetDtl5aOtherAssetLineItem> v)
    {
        this.other_assets = v;
    }

    private AssetDtl5aUndepositedFundsLineItem getUndepositedAt(int index)
    {
        if (undeposited_funds == null || undeposited_funds.size() <= index)
        {
            return null;
        }
        return undeposited_funds.get(index);
    }

    private java.lang.String getUndepositedLeftReason(int index)
    {
        AssetDtl5aUndepositedFundsLineItem item = getUndepositedAt(index);
        return item == null ? null : item.getSending_branch_or_reason_left();
    }

    private java.lang.String getUndepositedLeftAmount(int index)
    {
        AssetDtl5aUndepositedFundsLineItem item = getUndepositedAt(index);
        return item == null ? null : item.getAmount_left();
    }

    private java.lang.String getUndepositedRightReason(int index)
    {
        AssetDtl5aUndepositedFundsLineItem item = getUndepositedAt(index);
        return item == null ? null : item.getSending_branch_or_reason_right();
    }

    private java.lang.String getUndepositedRightDetail(int index)
    {
        AssetDtl5aUndepositedFundsLineItem item = getUndepositedAt(index);
        return item == null ? null : item.getSending_branch_or_reason_detail();
    }

    private java.lang.String getUndepositedRightAmount(int index)
    {
        AssetDtl5aUndepositedFundsLineItem item = getUndepositedAt(index);
        return item == null ? null : item.getAmount_right();
    }

    private AssetDtl5aReceivableLineItem getReceivableAt(int index)
    {
        if (receivables == null || receivables.size() <= index)
        {
            return null;
        }
        return receivables.get(index);
    }

    private java.lang.String getReceivableValue(
        int index,
        java.util.function.Function<AssetDtl5aReceivableLineItem, String> getter
    )
    {
        AssetDtl5aReceivableLineItem item = getReceivableAt(index);
        return item == null ? null : getter.apply(item);
    }

    private AssetDtl5aPrepaidExpenseLineItem getPrepaidExpenseAt(int index)
    {
        if (prepaid_expenses == null || prepaid_expenses.size() <= index)
        {
            return null;
        }
        return prepaid_expenses.get(index);
    }

    private java.lang.String getPrepaidExpenseValue(
        int index,
        java.util.function.Function<AssetDtl5aPrepaidExpenseLineItem, String> getter
    )
    {
        AssetDtl5aPrepaidExpenseLineItem item = getPrepaidExpenseAt(index);
        return item == null ? null : getter.apply(item);
    }

    private AssetDtl5aOtherAssetLineItem getOtherAssetAt(int index)
    {
        if (other_assets == null || other_assets.size() <= index)
        {
            return null;
        }
        return other_assets.get(index);
    }

    private java.lang.String getOtherAssetValue(
        int index,
        java.util.function.Function<AssetDtl5aOtherAssetLineItem, String> getter
    )
    {
        AssetDtl5aOtherAssetLineItem item = getOtherAssetAt(index);
        return item == null ? null : getter.apply(item);
    }
}
