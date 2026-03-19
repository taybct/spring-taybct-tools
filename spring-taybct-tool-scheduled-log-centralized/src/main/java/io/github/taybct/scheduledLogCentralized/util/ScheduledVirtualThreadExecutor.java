package io.github.taybct.scheduledLogCentralized.util;


import org.springframework.core.task.TaskDecorator;

import java.util.concurrent.Executor;

public class ScheduledVirtualThreadExecutor implements Executor {

    private final Executor delegate;
    private final TaskDecorator decorator;

    public ScheduledVirtualThreadExecutor(Executor delegate,
                                          TaskDecorator decorator) {
        this.delegate = delegate;
        this.decorator = decorator;
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(decorator.decorate(command));
    }
}