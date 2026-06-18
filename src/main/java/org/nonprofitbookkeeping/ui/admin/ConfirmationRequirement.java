package org.nonprofitbookkeeping.ui.admin;

/** Confirmation contract for destructive or irreversible administrative operations. */
public record ConfirmationRequirement(boolean required, String reason, String confirmationToken)
{
    public ConfirmationRequirement
    {
        if (required)
        {
            reason = ValidationMessage.requireText(reason, "reason");
            confirmationToken = ValidationMessage.requireText(confirmationToken, "confirmationToken");
        }
        else
        {
            reason = ValidationMessage.normalize(reason);
            confirmationToken = ValidationMessage.normalize(confirmationToken);
        }
    }

    public static ConfirmationRequirement none()
    {
        return new ConfirmationRequirement(false, null, null);
    }

    public static ConfirmationRequirement required(String reason, String confirmationToken)
    {
        return new ConfirmationRequirement(true, reason, confirmationToken);
    }
}
