package com.external.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpClient;
import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.workflow.Workflow;
import com.external.domain.Incident;
import com.external.service.ExceptionReasoningRequest;
import com.external.service.ExceptionReasoningResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ComponentId("IncidentWorkflow")
public class IncidentWorkflow extends Workflow<IncidentWorkflow.IncidentWorkflowState> {

    private static final Logger log = LoggerFactory.getLogger(IncidentWorkflow.class);
    private final ComponentClient componentClient;
    private final HttpClient reactAgentHttpClient;

    public IncidentWorkflow(ComponentClient componentClient, HttpClientProvider httpClientProvider) {
        this.componentClient = componentClient;
        this.reactAgentHttpClient = httpClientProvider.httpClientFor("ai-agentic-service");
    }

    public Effect<String> process(Incident incident) {
        log.debug("Processing incident {}", incident);
        IncidentWorkflowState incidentWorkflowState = new IncidentWorkflowState(incident.id());
        return effects().updateState(incidentWorkflowState).transitionTo("add_incident", incident).thenReply("incident-added");
    }

    @Override
    public WorkflowDef<IncidentWorkflowState> definition() {
        return workflow().addStep(addIncident());
    }

    private Step addIncident() {
        return step("add_incident")
                .call(Incident.class, cmd -> {
                    ExceptionReasoningRequest request = new ExceptionReasoningRequest(cmd.message());
                    ExceptionReasoningResponse post = reactAgentHttpClient.POST("/api/react-agent").withRequestBody(request).responseBodyAs(ExceptionReasoningResponse.class).invoke().body();
                    log.info("API call called");
                    Incident incident = new Incident(cmd.id(), cmd.message(), post.reference());
                    log.info("Adding incident {} to actor", incident);
                    return componentClient.forEventSourcedEntity("incidents").method(IncidentEntity::addIncident).invoke(incident);
                }).andThen(Incident.class, response ->
                    effects().end()
                );
    }


    public record IncidentWorkflowState(String incidentId) {
    }
}
