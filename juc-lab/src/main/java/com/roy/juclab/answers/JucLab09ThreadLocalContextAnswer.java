package com.roy.juclab.answers;

import com.roy.juclab.model.RequestContext;

import java.util.Objects;
import java.util.Optional;

public final class JucLab09ThreadLocalContextAnswer {
    private static final ThreadLocal<RequestContext> CURRENT =
            new ThreadLocal<>();

    private JucLab09ThreadLocalContextAnswer() {
    }

    public static void set(RequestContext context) {
        CURRENT.set(Objects.requireNonNull(context, "context"));
    }

    public static Optional<RequestContext> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Runnable wrap(Runnable task) {
        Objects.requireNonNull(task, "task");
        RequestContext captured = CURRENT.get();

        return () -> {
            RequestContext previous = CURRENT.get();
            try {
                install(captured);
                task.run();
            } finally {
                install(previous);
            }
        };
    }

    private static void install(RequestContext context) {
        if (context == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(context);
        }
    }
}
