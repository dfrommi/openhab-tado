package org.openhab.binding.tado.internal.api;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.terminationConditionTemplateToTerminationCondition;

import java.io.IOException;
import java.util.List;

import org.openhab.binding.tado.internal.ZoneUpdateException;
import org.openhab.binding.tado.internal.api.client.PUBLICApi;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.HomeInfo;
import org.openhab.binding.tado.internal.api.model.MobileDevice;
import org.openhab.binding.tado.internal.api.model.Overlay;
import org.openhab.binding.tado.internal.api.model.OverlayTemplate;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.User;
import org.openhab.binding.tado.internal.api.model.Zone;
import org.openhab.binding.tado.internal.api.model.ZoneState;

import retrofit2.Response;

public class TadoApiClient {
    private PUBLICApi api;

    public TadoApiClient(PUBLICApi api) {
        this.api = api;
    }

    public HomeInfo getHomeDetails(long homeId) throws IOException {
        return api.showHome(homeId).execute().body();
    }

    public User getUserDetails() throws IOException {
        return api.showUser().execute().body();
    }

    public List<Zone> listZones(long homeId) throws IOException {
        return api.listZones(homeId).execute().body();
    }

    public Zone getZoneDetails(long homeId, long zoneId) throws IOException {
        return api.showZoneDetails(homeId, zoneId).execute().body();
    }

    public ZoneState getZoneState(long homeId, long zoneId) throws IOException {
        return api.showZoneState(homeId, zoneId).execute().body();
    }

    public GenericZoneCapabilities getZoneCapabilities(long homeId, long zoneId) throws IOException {
        return api.showZoneCapabilities(homeId, zoneId).execute().body();
    }

    public OverlayTerminationCondition getDefaultTerminationCondition(long homeId, long zoneId) throws IOException {
        Response<OverlayTemplate> overlayTemplateResponse = api.showZoneDefaultOverlay(homeId, zoneId).execute();

        if (overlayTemplateResponse.isSuccessful()) {
            return terminationConditionTemplateToTerminationCondition(
                    overlayTemplateResponse.body().getTerminationCondition());
        }

        return null;
    }

    public Overlay setOverlay(long homeId, long zoneId, Overlay overlay) throws IOException, ZoneUpdateException {
        Response<Overlay> response = api.updateZoneOverlay(homeId, zoneId, overlay).execute();
        if (!response.isSuccessful()) {
            throw new ZoneUpdateException(
                    "Could not change HVAC setting of zone " + zoneId + ": " + response.errorBody().string());
        }

        return response.body();
    }

    public void removeOverlay(long homeId, long zoneId) throws IOException, ZoneUpdateException {
        Response<Void> response = api.deleteZoneOverlay(homeId, zoneId).execute();
        if (!response.isSuccessful()) {
            throw new ZoneUpdateException(
                    "Could not remove overlay of zone " + zoneId + ": " + response.errorBody().string());
        }
    }

    public List<MobileDevice> listMobileDevices(long homeId) throws IOException {
        return api.listMobileDevices(homeId).execute().body();
    }

    public MobileDevice getMobileDeviceDetails(long homeId, int mobileDeviceId) throws IOException {
        Response<List<MobileDevice>> mobileDeviceResponse = api.listMobileDevices(homeId).execute();
        if (mobileDeviceResponse.isSuccessful()) {
            return mobileDeviceResponse.body().stream().filter(m -> m.getId() == mobileDeviceId).findFirst()
                    .orElse(null);
        }

        return null;
    }
}
