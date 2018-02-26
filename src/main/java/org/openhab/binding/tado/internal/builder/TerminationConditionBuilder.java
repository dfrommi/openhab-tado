package org.openhab.binding.tado.internal.builder;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.*;

import java.io.IOException;

import org.openhab.binding.tado.handler.TadoZoneHandler;
import org.openhab.binding.tado.internal.api.TadoClientException;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.api.model.TimerTerminationCondition;
import org.openhab.binding.tado.internal.api.model.ZoneState;

/**
 * Builder for creation of overlay termination conditions.
 *
 * @author Dennis Frommknecht - Iniital contribution
 */
public class TerminationConditionBuilder {
    private TadoZoneHandler zoneHandler;

    private OverlayTerminationConditionType terminationType = null;
    private Integer timerDurationInSeconds = null;

    protected TerminationConditionBuilder(TadoZoneHandler zoneHandler) {
        this.zoneHandler = zoneHandler;
    }

    public static TerminationConditionBuilder of(TadoZoneHandler zoneHandler) {
        return new TerminationConditionBuilder(zoneHandler);
    }

    public TerminationConditionBuilder withTerminationType(OverlayTerminationConditionType terminationType) {
        this.terminationType = terminationType;
        if (terminationType != OverlayTerminationConditionType.TIMER) {
            timerDurationInSeconds = null;
        }

        return this;
    }

    public TerminationConditionBuilder withTimerDurationInSeconds(Integer timerDurationInSeconds) {
        this.terminationType = OverlayTerminationConditionType.TIMER;
        this.timerDurationInSeconds = timerDurationInSeconds;
        return this;
    }

    public OverlayTerminationCondition build(ZoneState zoneState) throws IOException, TadoClientException {
        OverlayTerminationCondition terminationCondition = null;

        if (terminationType != null) {
            if (terminationType != OverlayTerminationConditionType.TIMER || timerDurationInSeconds != null) {
                terminationCondition = getTerminationCondition(terminationType, timerDurationInSeconds);
            } else {
                terminationCondition = getCurrentOrDefaultTimerTermination(zoneState);
            }
        } else {
            if (zoneState.getOverlay() != null) {
                terminationCondition = cleanTerminationCondition(zoneState.getOverlay().getTermination());
            } else {
                // Default zone termination condition
                terminationCondition = getDefaultTerminationCondition();
            }
        }
        return terminationCondition;
    }

    private OverlayTerminationCondition getDefaultTerminationCondition() throws IOException, TadoClientException {
        OverlayTerminationCondition defaultTerminationCondition = zoneHandler.getDefaultTerminationCondition();
        return defaultTerminationCondition != null ? defaultTerminationCondition : manualTermination();
    }

    private TimerTerminationCondition getCurrentOrDefaultTimerTermination(ZoneState zoneState) throws IOException {
        // Timer without duration
        int duration = zoneHandler.getFallbackTimerDuration() * 60;

        // If timer is currently running, use its time
        if (zoneState.getOverlay() != null
                && zoneState.getOverlay().getTermination().getType() == OverlayTerminationConditionType.TIMER) {
            duration = ((TimerTerminationCondition) zoneState.getOverlay().getTermination()).getDurationInSeconds();
        }

        return timerTermination(duration);
    }
}
