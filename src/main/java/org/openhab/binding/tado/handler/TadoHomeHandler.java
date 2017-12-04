/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.handler;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.tado.TadoBindingConstants;
import org.openhab.binding.tado.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.config.TadoHomeConfig;
import org.openhab.binding.tado.internal.api.TadoApiClient;
import org.openhab.binding.tado.internal.api.TadoApiClientFactory;
import org.openhab.binding.tado.internal.api.model.HomeInfo;
import org.openhab.binding.tado.internal.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TadoHomeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TadoHomeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(TadoHomeHandler.class);

    private TadoHomeConfig configuration;
    private TadoApiClient api;
    private Long homeId;

    public TadoHomeHandler(Bridge bridge) {
        super(bridge);
    }

    public TemperatureUnit getTemperatureUnit() {
        String temperatureUnitStr = this.thing.getProperties().get(TadoBindingConstants.PROPERTY_HOME_TEMPERATURE_UNIT);
        return TemperatureUnit.valueOf(temperatureUnitStr);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(TadoHomeConfig.class);
        api = new TadoApiClientFactory().create(configuration.username, configuration.password);

        try {
            // Get user info to verify successful authentication and connection to server
            User user = api.getUserDetails();
            if (user == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Cannot connect to server. Username and/or password might be invalid");
                return;
            }

            if (user.getHomes().isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "User does not have access to any home");
                return;
            }

            homeId = user.getHomes().get(0).getId().longValue();

            HomeInfo homeInfo = api.getHomeDetails(homeId);
            TemperatureUnit temperatureUnit = org.openhab.binding.tado.internal.api.model.TemperatureUnit.FAHRENHEIT == homeInfo
                    .getTemperatureUnit() ? TemperatureUnit.FAHRENHEIT : TemperatureUnit.CELSIUS;
            updateProperty(TadoBindingConstants.PROPERTY_HOME_TEMPERATURE_UNIT, temperatureUnit.name());
        } catch (IOException e) {
            logger.error("Error accessing tado server: " + e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not connect to server due to " + e.getMessage());
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public TadoApiClient getApi() {
        return api;
    }

    public Long getHomeId() {
        return homeId;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do for a bridge
    }
}
