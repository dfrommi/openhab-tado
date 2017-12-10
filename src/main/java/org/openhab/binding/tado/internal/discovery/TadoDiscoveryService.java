package org.openhab.binding.tado.internal.discovery;

import static org.openhab.binding.tado.TadoBindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.tado.TadoBindingConstants;
import org.openhab.binding.tado.handler.TadoHomeHandler;
import org.openhab.binding.tado.internal.api.TadoClientException;
import org.openhab.binding.tado.internal.api.model.MobileDevice;
import org.openhab.binding.tado.internal.api.model.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class TadoDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(TadoDiscoveryService.class);

    public final static Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ZONE,
            THING_TYPE_MOBILE_DEVICE);

    private TadoHomeHandler homeHandler;

    public TadoDiscoveryService(TadoHomeHandler tadoHomeHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, 5);
        this.homeHandler = tadoHomeHandler;
    }

    @Override
    protected void startScan() {
        if (homeHandler.getHomeId() == null) {
            return;
        }

        discoverZones();
        discoverMobileDevices();
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        startScan();
    }

    private void discoverZones() {
        Long homeId = homeHandler.getHomeId();
        try {
            List<Zone> zoneList = homeHandler.getApi().listZones(homeId);

            if (zoneList != null) {
                for (Zone zone : zoneList) {
                    notifyZoneDiscovery(homeId, zone);
                }
            }
        } catch (IOException | TadoClientException e) {
            logger.error("Could not discover tado zones: {}", e.getMessage(), e);
        }
    }

    private void notifyZoneDiscovery(Long homeId, Zone zone) {
        Integer zoneId = zone.getId();

        ThingUID bridgeUID = this.homeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(TadoBindingConstants.THING_TYPE_ZONE, bridgeUID, zoneId.toString());

        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_ZONE_ID, zoneId);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(zone.getName())
                .withProperties(properties).build();

        thingDiscovered(result);

        logger.debug("Discovered zone '{}' with id {} ({})", zone.getName(), zoneId.toString(), uid);
    }

    private void discoverMobileDevices() {
        Long homeId = homeHandler.getHomeId();
        try {
            List<MobileDevice> mobileDeviceList = homeHandler.getApi().listMobileDevices(homeId);

            if (mobileDeviceList != null) {
                for (MobileDevice mobileDevice : mobileDeviceList) {
                    if (mobileDevice.getSettings().getGeoTrackingEnabled()) {
                        notifyMobileDeviceDiscovery(homeId, mobileDevice);
                    }
                }
            }
        } catch (IOException | TadoClientException e) {
            logger.error("Could not discover tado zones: {}", e.getMessage(), e);
        }
    }

    private void notifyMobileDeviceDiscovery(Long homeId, MobileDevice device) {
        ThingUID bridgeUID = this.homeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(TadoBindingConstants.THING_TYPE_MOBILE_DEVICE, bridgeUID,
                device.getId().toString());

        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_MOBILE_DEVICE_ID, device.getId());

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(device.getName())
                .withProperties(properties).build();

        thingDiscovered(result);

        logger.debug("Discovered mobile device '{}' with id {} ({})", device.getName(), device.getId().toString(), uid);
    }
}
