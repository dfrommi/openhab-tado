package org.openhab.binding.tado.internal.api.converter;

import java.lang.reflect.Type;

import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionTemplate;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.api.model.TimerTerminationConditionTemplate;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OverlayTerminationConditionTemplateConverter implements
        JsonSerializer<OverlayTerminationConditionTemplate>, JsonDeserializer<OverlayTerminationConditionTemplate> {
    @Override
    public JsonElement serialize(OverlayTerminationConditionTemplate src, Type srcType,
            JsonSerializationContext context) {
        if (src instanceof TimerTerminationConditionTemplate) {
            return context.serialize(src, TimerTerminationConditionTemplate.class);
        }

        return context.serialize(src, OverlayTerminationConditionTemplate.class);
    }

    @Override
    public OverlayTerminationConditionTemplate deserialize(JsonElement json, Type type,
            JsonDeserializationContext context) throws JsonParseException {

        OverlayTerminationConditionType terminationType = OverlayTerminationConditionType
                .valueOf(json.getAsJsonObject().get("type").getAsString());

        if (terminationType == OverlayTerminationConditionType.TIMER) {
            return context.deserialize(json, TimerTerminationConditionTemplate.class);
        }

        // no converter, otherwise stackoverflow
        return new GsonBuilder().create().fromJson(json, OverlayTerminationConditionTemplate.class);
    }
}
