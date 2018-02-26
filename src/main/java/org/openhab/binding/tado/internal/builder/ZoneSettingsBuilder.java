package org.openhab.binding.tado.internal.builder;

import org.openhab.binding.tado.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.handler.TadoZoneHandler;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.api.model.ZoneState;

/**
 * Base class for zone settings builder.
 *
 * @author Dennis Frommknecht - Iniital contribution
 */
public abstract class ZoneSettingsBuilder {
    public static ZoneSettingsBuilder of(TadoZoneHandler zoneHandler) {
        switch (zoneHandler.getZoneType()) {
            case HEATING:
                return new HeatingZoneSettingsBuilder();
            case AIR_CONDITIONING:
                return new AirConditioningZoneSettingsBuilder();
            case HOT_WATER:
                return new HotWaterZoneSettingsBuilder();
            default:
                throw new IllegalArgumentException("Zone type " + zoneHandler.getZoneType() + " unknown");
        }
    }

    protected HvacMode mode = null;
    protected Float temperature = null;
    protected TemperatureUnit temperatureUnit = TemperatureUnit.CELSIUS;
    protected Boolean swing = null;
    protected FanSpeed fanSpeed = null;

    public ZoneSettingsBuilder withMode(HvacMode mode) {
        this.mode = mode;
        return this;
    }

    public ZoneSettingsBuilder withTemperature(Float temperature, TemperatureUnit temperatureUnit) {
        this.temperature = temperature;
        this.temperatureUnit = temperatureUnit;
        return this;
    }

    public ZoneSettingsBuilder withSwing(boolean swingOn) {
        this.swing = swingOn;
        return this;
    }

    public ZoneSettingsBuilder withFanSpeed(FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
        return this;
    }

    public abstract GenericZoneSetting build(ZoneState zoneState, GenericZoneCapabilities capabilities);

    protected TemperatureObject truncateTemperature(TemperatureObject temperature) {
        if (temperature == null) {
            return null;
        }

        TemperatureObject temperatureObject = new TemperatureObject();
        if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            temperatureObject.setFahrenheit(temperature.getFahrenheit());
        } else {
            temperatureObject.setCelsius(temperature.getCelsius());
        }

        return temperatureObject;
    }

    protected TemperatureObject buildDefaultTemperatureObject(float temperatureCelsius, float temperatureFahrenheit) {
        TemperatureObject temperatureObject = new TemperatureObject();

        if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            temperatureObject.setFahrenheit(temperatureFahrenheit);
        } else {
            temperatureObject.setCelsius(temperatureCelsius);
        }

        return temperatureObject;
    }
}
