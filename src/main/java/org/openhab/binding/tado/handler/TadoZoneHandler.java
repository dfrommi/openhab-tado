/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.tado.TadoBindingConstants;
import org.openhab.binding.tado.TadoBindingConstants.OperationMode;
import org.openhab.binding.tado.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.TadoBindingConstants.ZoneType;
import org.openhab.binding.tado.config.TadoZoneConfig;
import org.openhab.binding.tado.internal.TadoHvacChange;
import org.openhab.binding.tado.internal.adapter.TadoZoneStateAdapter;
import org.openhab.binding.tado.internal.api.TadoClientException;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.Overlay;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.Zone;
import org.openhab.binding.tado.internal.api.model.ZoneState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TadoZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoZoneHandler extends BaseHomeThingHandler {
    private Logger logger = LoggerFactory.getLogger(TadoZoneHandler.class);

    private TadoZoneConfig configuration;
    private ScheduledFuture<?> refreshTimer;
    private ScheduledFuture<?> scheduledHvacChange;
    private GenericZoneCapabilities capabilities;

    TadoHvacChange pendingHvacChange;

    public TadoZoneHandler(Thing thing) {
        super(thing);
    }

    public long getZoneId() {
        return this.configuration.id;
    }

    public int getFallbackTimerDuration() {
        return this.configuration.fallbackTimerDuration;
    }

    public ZoneType getZoneType() {
        String zoneTypeStr = this.thing.getProperties().get(TadoBindingConstants.PROPERTY_ZONE_TYPE);
        return ZoneType.valueOf(zoneTypeStr);
    }

    public OverlayTerminationCondition getDefaultTerminationCondition() throws IOException, TadoClientException {
        return getApi().getDefaultTerminationCondition(getHomeId(), getZoneId());
    }

    public ZoneState getZoneState() throws IOException, TadoClientException {
        return getApi().getZoneState(getHomeId(), getZoneId());
    }

    public GenericZoneCapabilities getZoneCapabilities() {
        return this.capabilities;
    }

    public TemperatureUnit getTemperatureUnit() {
        return getHomeHandler().getTemperatureUnit();
    }

    public Overlay setOverlay(Overlay overlay) throws IOException, TadoClientException {
        logger.debug("Setting overlay of home " + getHomeId() + " and zone " + getZoneId());
        return getApi().setOverlay(getHomeId(), getZoneId(), overlay);
    }

    public void removeOverlay() throws IOException, TadoClientException {
        logger.debug("Removing overlay of home " + getHomeId() + " and zone " + getZoneId());
        getApi().removeOverlay(getHomeId(), getZoneId());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();

        if (command == RefreshType.REFRESH) {
            updateZoneState(false);
            return;
        }

        switch (id) {
            case TadoBindingConstants.CHANNEL_ZONE_HVAC_MODE:
                pendingHvacChange.withHvacMode(((StringType) command).toFullString());
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_TARGET_TEMPERATURE:
                pendingHvacChange.withTemperature(((DecimalType) command).floatValue());
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_SWING:
                pendingHvacChange.withSwing(((OnOffType) command) == OnOffType.ON);
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_FAN_SPEED:
                pendingHvacChange.withFanSpeed(((StringType) command).toFullString());
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_OPERATION_MODE:
                String operationMode = ((StringType) command).toFullString();
                pendingHvacChange.withOperationMode(OperationMode.valueOf(operationMode));
                scheduleHvacChange();
                break;
            case TadoBindingConstants.CHANNEL_ZONE_TIMER_DURATION:
                pendingHvacChange.activeFor(((DecimalType) command).intValue());
                scheduleHvacChange();
                break;
        }
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(TadoZoneConfig.class);

        if (getBridge() != null) {
            bridgeStatusChanged(getBridge().getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            try {
                Zone zoneDetails = getApi().getZoneDetails(getHomeId(), getZoneId());
                GenericZoneCapabilities capabilities = getApi().getZoneCapabilities(getHomeId(), getZoneId());

                if (zoneDetails == null || capabilities == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Can not access zone " + getZoneId() + " of home " + getHomeId());
                    return;
                }

                updateProperty(TadoBindingConstants.PROPERTY_ZONE_NAME, zoneDetails.getName());
                updateProperty(TadoBindingConstants.PROPERTY_ZONE_TYPE, zoneDetails.getType().name());
                this.capabilities = capabilities;
            } catch (IOException | TadoClientException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Could not connect to server due to " + e.getMessage());
                cancelScheduledZoneStateUpdate();
                return;
            }

            scheduleZoneStateUpdate();
            pendingHvacChange = new TadoHvacChange(getThing());

            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            cancelScheduledZoneStateUpdate();
        }
    }

    private void updateZoneState(boolean forceUpdate) {
        // No update during HVAC change debounce
        if (!forceUpdate && scheduledHvacChange != null && !scheduledHvacChange.isDone()) {
            return;
        }

        try {
            ZoneState zoneState = getZoneState();
            if (zoneState == null) {
                return;
            }

            logger.debug("Updating state of home " + getHomeId() + " and zone " + getZoneId());

            TadoZoneStateAdapter state = new TadoZoneStateAdapter(zoneState, getTemperatureUnit());
            updateStateIfNotNull(TadoBindingConstants.CHANNEL_ZONE_CURRENT_TEMPERATURE, state.getInsideTemperature());
            updateStateIfNotNull(TadoBindingConstants.CHANNEL_ZONE_HUMIDITY, state.getHumidity());
            updateStateIfNotNull(TadoBindingConstants.CHANNEL_ZONE_HEATING_POWER, state.getHeatingPower());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OPERATION_MODE, state.getOperationMode());

            updateState(TadoBindingConstants.CHANNEL_ZONE_HVAC_MODE, state.getMode());
            updateState(TadoBindingConstants.CHANNEL_ZONE_TARGET_TEMPERATURE, state.getTargetTemperature());
            updateState(TadoBindingConstants.CHANNEL_ZONE_FAN_SPEED, state.getFanSpeed());
            updateState(TadoBindingConstants.CHANNEL_ZONE_SWING, state.getSwing());

            updateState(TadoBindingConstants.CHANNEL_ZONE_TIMER_DURATION, state.getRemainingTimerDuration());

            updateState(TadoBindingConstants.CHANNEL_ZONE_OVERLAY_EXPIRY, state.getOverlayExpiration());
        } catch (IOException | TadoClientException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to server due to " + e.getMessage());
        }
    }

    private void scheduleZoneStateUpdate() {
        if (refreshTimer == null || refreshTimer.isCancelled()) {
            refreshTimer = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    updateZoneState(false);
                }
            }, 5, configuration.refreshInterval, TimeUnit.SECONDS);
        }
    }

    private void cancelScheduledZoneStateUpdate() {
        if (refreshTimer != null) {
            refreshTimer.cancel(false);
        }
    }

    private void scheduleHvacChange() {
        if (scheduledHvacChange != null) {
            scheduledHvacChange.cancel(false);
        }

        scheduledHvacChange = scheduler.schedule(() -> {
            try {
                TadoHvacChange change = this.pendingHvacChange;
                this.pendingHvacChange = new TadoHvacChange(getThing());
                change.apply();
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (TadoClientException e) {
                logger.error("Could not apply HVAC change on home + " + getHomeId() + " and zone " + getZoneId() + ": "
                        + e.getMessage(), e);
            } finally {
                updateZoneState(true);
            }
        }, configuration.hvacChangeDebounce, TimeUnit.SECONDS);
    }

    private void updateStateIfNotNull(String channelID, State state) {
        if (state != null) {
            updateState(channelID, state);
        }
    }

}
