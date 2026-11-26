package org.example.back_end1.auditing;

public final class BatchImportActorContext {

    private static final ThreadLocal<String> CURRENT_ACTOR = new ThreadLocal<>();

    private BatchImportActorContext() {}

    public static void setActor(String actor) {
        if (actor != null && !actor.isBlank()) {
            CURRENT_ACTOR.set(actor);
        } else {
            CURRENT_ACTOR.remove();
        }
    }

    public static String getActor() {
        return CURRENT_ACTOR.get();
    }

    public static void clear() {
        CURRENT_ACTOR.remove();
    }
}