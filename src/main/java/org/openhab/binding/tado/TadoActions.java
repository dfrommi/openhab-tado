package org.openhab.binding.tado;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.tado.internal.TadoHvacChange;

public class TadoActions {

    public static TadoHvacChange hvacChange(Thing thing) throws IOException {
        return new TadoHvacChange(thing);
    }

    public static TadoHvacChange hvacChange(ThingRegistry things, String name) throws IOException {
        return hvacChange(things.get(new ThingUID(name)));
    }
}
