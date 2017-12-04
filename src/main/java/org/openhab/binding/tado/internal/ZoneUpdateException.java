package org.openhab.binding.tado.internal;

public class ZoneUpdateException extends Exception {
    public ZoneUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZoneUpdateException(String message) {
        super(message);
    }
}
