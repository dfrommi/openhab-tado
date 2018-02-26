package org.openhab.binding.tado.config;

/**
 * Holder-object for zone configuration
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoZoneConfig {
    public long id;
    public int refreshInterval;
    public int fallbackTimerDuration;
    public int hvacChangeDebounce;
}
