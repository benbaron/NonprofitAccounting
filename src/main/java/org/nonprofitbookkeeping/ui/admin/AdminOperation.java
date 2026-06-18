package org.nonprofitbookkeeping.ui.admin;

import java.util.function.Consumer;

/** Service interface for long-running alternate UI administrative operations. */
public interface AdminOperation<R>
{
    AdminOperationResult preview(R request);

    AdminOperationResult commit(R request, Consumer<OperationProgress> progressConsumer);
}
