package nonprofitbookkeeping.service;

final class DepreciationPostingRoles
{
    private DepreciationPostingRoles() {}

    static String expenseCode()
    {
        return System.getProperty("nonprofitbookkeeping.depreciation.expenseAccountCode",
            "DEPRECIATION_EXPENSE");
    }

    static String accumulatedDepreciationCode()
    {
        return System.getProperty("nonprofitbookkeeping.depreciation.accumulatedAccountCode",
            "ACCUMULATED_DEPRECIATION");
    }
}
