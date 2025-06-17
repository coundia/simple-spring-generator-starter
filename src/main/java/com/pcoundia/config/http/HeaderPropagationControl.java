package com.pcoundia.config.http;

public class HeaderPropagationControl {

    private static final ThreadLocal<Boolean> shouldPropagate = ThreadLocal.withInitial(() -> true);

    public static void enableHeaderPropagation() {
        shouldPropagate.set(true);
    }

    public static void disableHeaderPropagation() {
        shouldPropagate.set(false);
    }

    public static boolean shouldPropagateHeaders() {
        return shouldPropagate.get();
    }
}
