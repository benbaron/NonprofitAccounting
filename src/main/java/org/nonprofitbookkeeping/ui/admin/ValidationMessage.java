package org.nonprofitbookkeeping.ui.admin;

import java.util.Objects;

/** One user-facing validation or result message for an administrative operation. */
public record ValidationMessage(ValidationSeverity severity, String code, String message, String location)
{
    public ValidationMessage
    {
        severity = Objects.requireNonNull(severity, "severity");
        message = requireText(message, "message");
        code = normalize(code);
        location = normalize(location);
    }

    public static ValidationMessage info(String code, String message)
    {
        return new ValidationMessage(ValidationSeverity.INFO, code, message, null);
    }

    public static ValidationMessage warning(String code, String message)
    {
        return new ValidationMessage(ValidationSeverity.WARNING, code, message, null);
    }

    public static ValidationMessage error(String code, String message)
    {
        return new ValidationMessage(ValidationSeverity.ERROR, code, message, null);
    }

    public static ValidationMessage blocking(String code, String message)
    {
        return new ValidationMessage(ValidationSeverity.BLOCKING, code, message, null);
    }

    public boolean isBlocking()
    {
        return this.severity.isBlocking();
    }

    static String requireText(String value, String name)
    {
        if (value == null || value.isBlank())
        {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    static String normalize(String value)
    {
        return value == null || value.isBlank() ? null : value;
    }
}
