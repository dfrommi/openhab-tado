package org.openhab.binding.tado.internal.builder;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.temperature;

import org.openhab.binding.tado.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.HeatingZoneSetting;
import org.openhab.binding.tado.internal.api.model.Power;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.api.model.ZoneState;

public class HeatingZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final float DEFAULT_TEMPERATURE_C = 22.0f;
    private static final float DEFAULT_TEMPERATURE_F = 72.0f;

    @Override
    public ZoneSettingsBuilder withSwing(boolean swingOn) {
        throw new IllegalArgumentException("Heating zones don't support SWING");
    }

    @Override
    public ZoneSettingsBuilder withFanSpeed(FanSpeed fanSpeed) {
        throw new IllegalArgumentException("Heating zones don't support FAN SPEED");
    }

    @Override
    public GenericZoneSetting build(ZoneState zoneState, GenericZoneCapabilities capabilities) {
        if (mode == HvacMode.OFF) {
            return heatingSetting(false);
        }

        HeatingZoneSetting setting = heatingSetting(true);

        if (temperature != null) {
            setting.setTemperature(temperature(temperature, temperatureUnit));
        }

        addMissingSettingParts(setting, zoneState);

        return setting;
    }

    private void addMissingSettingParts(HeatingZoneSetting setting, ZoneState zoneState) {
        if (setting.getTemperature() == null) {
            TemperatureObject temperatureObject = getCurrentOrDefaultTemperature(zoneState);
            setting.setTemperature(temperatureObject);
        }
    }

    private TemperatureObject getCurrentOrDefaultTemperature(ZoneState zoneState) {
        HeatingZoneSetting zoneSetting = (HeatingZoneSetting) zoneState.getSetting();

        if (zoneSetting != null && zoneSetting.getTemperature() != null) {
            return truncateTemperature(zoneSetting.getTemperature());
        }

        return buildDefaultTemperatureObject(DEFAULT_TEMPERATURE_C, DEFAULT_TEMPERATURE_F);
    }

    private HeatingZoneSetting heatingSetting(boolean powerOn) {
        HeatingZoneSetting setting = new HeatingZoneSetting();
        setting.setType(TadoSystemType.HEATING);
        setting.setPower(powerOn ? Power.ON : Power.OFF);
        return setting;
    }
}
