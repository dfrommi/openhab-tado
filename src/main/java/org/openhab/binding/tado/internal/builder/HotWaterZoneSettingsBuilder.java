package org.openhab.binding.tado.internal.builder;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.temperature;

import org.openhab.binding.tado.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.HotWaterCapabilities;
import org.openhab.binding.tado.internal.api.model.HotWaterZoneSetting;
import org.openhab.binding.tado.internal.api.model.Power;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.api.model.ZoneState;

public class HotWaterZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final float DEFAULT_TEMPERATURE_C = 50.0f;
    private static final float DEFAULT_TEMPERATURE_F = 122.0f;

    @Override
    public ZoneSettingsBuilder withSwing(boolean swingOn) {
        throw new IllegalArgumentException("Hot Water zones don't support SWING");
    }

    @Override
    public ZoneSettingsBuilder withFanSpeed(FanSpeed fanSpeed) {
        throw new IllegalArgumentException("Hot Water zones don't support FAN SPEED");
    }

    @Override
    public GenericZoneSetting build(ZoneState zoneState, GenericZoneCapabilities capabilities) {
        if (mode == HvacMode.OFF) {
            return hotWaterSetting(false);
        }

        HotWaterZoneSetting setting = hotWaterSetting(true);

        if (temperature != null) {
            setting.setTemperature(temperature(temperature, temperatureUnit));
        }

        addMissingSettingParts(setting, zoneState, (HotWaterCapabilities) capabilities);

        return setting;
    }

    private void addMissingSettingParts(HotWaterZoneSetting setting, ZoneState zoneState,
            HotWaterCapabilities capabilities) {

        if (capabilities.getCanSetTemperature() && setting.getTemperature() == null) {
            TemperatureObject temperatureObject = getCurrentOrDefaultTemperature(zoneState);
            setting.setTemperature(temperatureObject);
        }
    }

    private TemperatureObject getCurrentOrDefaultTemperature(ZoneState zoneState) {
        HotWaterZoneSetting zoneSetting = (HotWaterZoneSetting) zoneState.getSetting();

        if (zoneSetting != null && zoneSetting.getTemperature() != null) {
            return truncateTemperature(zoneSetting.getTemperature());
        }

        return buildDefaultTemperatureObject(DEFAULT_TEMPERATURE_C, DEFAULT_TEMPERATURE_F);
    }

    private HotWaterZoneSetting hotWaterSetting(boolean powerOn) {
        HotWaterZoneSetting setting = new HotWaterZoneSetting();
        setting.setType(TadoSystemType.HOT_WATER);
        setting.setPower(powerOn ? Power.ON : Power.OFF);
        return setting;
    }
}
