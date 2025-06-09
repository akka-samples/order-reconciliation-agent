package com.external.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.TypeName;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import com.external.domain.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ComponentId("incident")
public class IncidentEntity extends EventSourcedEntity<IncidentEntity.IncidentState, IncidentEntity.IncidentEvent> {

    private static final Logger log = LoggerFactory.getLogger(IncidentEntity.class);


    public Effect<Incident> addIncident(Incident incident) {
        return effects()
                .persist(new IncidentEvent.IncidentAdded(incident.id(), incident))
                .thenReply(incidentState -> incident);
    }

    public ReadOnlyEffect<Collection<Incident>> getIncidents() {
        return effects().reply(currentState().incidents().values());
    }

    @Override
    public IncidentState applyEvent(IncidentEvent event) {
        return switch (event) {
            case IncidentEvent.IncidentAdded evt -> currentState()
                    .addIncident(evt.incident());
        };
    }

    @Override
    public IncidentState emptyState() {
        return new IncidentState(new HashMap<>());
    }


    public record IncidentState(Map<String, Incident> incidents) {
        public IncidentState addIncident(Incident incident) {
            incidents.put(incident.id(), incident);
            return new IncidentState(incidents);
        }
    }

    public sealed interface IncidentEvent {
        @TypeName("incident-added")
        record IncidentAdded(String id, Incident incident) implements IncidentEntity.IncidentEvent {
        }

    }
}
