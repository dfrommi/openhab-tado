package org.openhab.binding.tado.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.tado.internal.api.TadoApiClient;

/**
 * Common base class for home-based thing-handler.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
abstract public class BaseHomeThingHandler extends BaseThingHandler {

    public BaseHomeThingHandler(Thing thing) {
        super(thing);
    }

    public Long getHomeId() {
        return getHomeHandler().getHomeId();
    }

    protected TadoHomeHandler getHomeHandler() {
        return (TadoHomeHandler) getBridge().getHandler();
    }

    protected TadoApiClient getApi() {
        return getHomeHandler().getApi();
    }
}
