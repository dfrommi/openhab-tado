package org.openhab.binding.tado.internal.api;

/**
 * Custom exception for logical errors on the API.
 *
 * @author Dennis Frommknecht - Iniital contribution
 */
public class TadoClientException extends Exception {
    public TadoClientException() {
    }

    public TadoClientException(String message) {
        super(message);
    }

    public TadoClientException(Throwable cause) {
        super(cause);
    }

    public TadoClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public TadoClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
