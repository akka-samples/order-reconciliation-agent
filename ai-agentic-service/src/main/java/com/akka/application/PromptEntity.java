package com.akka.application;


import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("prompt-store")
public class PromptEntity extends EventSourcedEntity<PromptEntity.State, PromptEntity.Event> {

    private static final Logger log = LoggerFactory.getLogger(PromptEntity.class);
    
    public PromptEntity() {

    }

    public Effect<PromptTemplate> addPromptTemplate(PromptTemplate cmd) {
        log.debug("storing new prompt template {}", cmd);
        return effects()
                .persist(new Event.PromptTemplateAdded(cmd.name(), cmd.content()))
                .thenReply(state -> new PromptTemplate(cmd.name(), cmd.content()));
    }

    public ReadOnlyEffect<PromptTemplate> getPromptTemplate() {
        return effects().reply(currentState().template());
    }

    @Override
    public State emptyState() {
        return new State(new PromptTemplate("", ""));
    }


    @Override
    public State applyEvent(Event event) {
        return switch (event) {
            case Event.PromptTemplateAdded evt -> updateStatePrompt(currentState(), evt);
            default -> throw new IllegalStateException("Unexpected value: " + event);
        };
    }


    public State updateStatePrompt(State state, Event.PromptTemplateAdded evt) {
        return new State(new PromptTemplate(evt.name(), evt.content()));
    }

    public record PromptTemplate(String name, String content) {
    }

    public record State(PromptTemplate template) {
    }

    public sealed interface Event {

        public record PromptTemplateAdded(String name, String content) implements Event {
        }
    }
}
