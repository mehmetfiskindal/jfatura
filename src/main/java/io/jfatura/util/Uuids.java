package io.jfatura.util;

import java.util.UUID;

/** GİB isteklerinde kullanılan {@code callid} üreticisi. */
public final class Uuids {

    private Uuids() {
    }

    public static String newCallId() {
        return UUID.randomUUID().toString();
    }
}
