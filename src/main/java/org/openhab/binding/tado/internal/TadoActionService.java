package org.openhab.binding.tado.internal;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.openhab.binding.tado.TadoActions;

public class TadoActionService implements ActionService {
    private static ThingRegistry thingRegistry;

    @Override
    public String getActionClassName() {
        return TadoActions.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return TadoActions.class;
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public void setThingRegistry(ThingRegistry thingRegistry) {
        TadoActionService.thingRegistry = thingRegistry;
    }

    public void unsetThingRegistry(ThingRegistry thingRegistry) {
        TadoActionService.thingRegistry = null;
    }

    public static TadoHvacChange getHvacChange(Thing zone) {
        return new TadoHvacChange(zone);
    }

    public static TadoHvacChange getHvacChange(String zoneThingName) {
        Thing thing = thingRegistry.get(new ThingUID(zoneThingName));
        return getHvacChange(thing);
    }
}
