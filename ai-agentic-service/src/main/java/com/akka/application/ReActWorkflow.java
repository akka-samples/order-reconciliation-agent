package com.akka.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpClientProvider;
import akka.javasdk.workflow.Workflow;
import com.akka.application.command.ProcessIncident;
import com.akka.application.model.ChainOfThought;
import com.akka.application.state.ContextForLLM;
import com.akka.application.state.WorkFlowState;
import com.akka.domain.Conversation;
import com.akka.service.AiService;
import com.akka.service.ToolExecutionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ComponentId("ReActWorkflow")
public class ReActWorkflow extends Workflow<WorkFlowState> {

    private static final Logger log = LoggerFactory.getLogger(ReActWorkflow.class);
    public static final String OBSERVATION_OUTPUT_FORMAT = "{\"Observation\": \"%s\" }";

    final private ComponentClient componentClient;
    private final String promptTemplateName = "react-workflow-prompt";

    private final AiService aiService;
    private final ToolExecutionService toolExecutionService;

    public ReActWorkflow(ComponentClient componentClient, Config config, HttpClientProvider httpClientProvider) {
        this.componentClient = componentClient;
        this.toolExecutionService = new ToolExecutionService(config,httpClientProvider);
        this.aiService = new AiService();
    }

    public Effect<String> solveIncident(ProcessIncident command) {
        log.debug("Starting ReAct workflow for {}", command);
        WorkFlowState initialState = new WorkFlowState(new ArrayList<>(), WorkFlowState.AgentStatus.INITIATED);
        return effects()
                .updateState(initialState)
                .transitionTo("prepare_agent", command)
                .thenReply(command.agentId());
    }

    public ReadOnlyEffect<List<Conversation>> getConversations() {
        List<Conversation> conversations = currentState().context() == null ? List.of() : currentState().context();
        return effects().reply(conversations.size() > 1 ? conversations.subList(1, conversations.size()) : conversations);
    }

    @Override
    public WorkflowDef<WorkFlowState> definition() {
        return workflow()
                .addStep(prepare())
                .addStep(callLLM())
                .addStep(toolExecution())
                .timeout(Duration.of(10, ChronoUnit.SECONDS));
    }

    private Step prepare() {
        return step("prepare_agent")
                .call(ProcessIncident.class, cmd -> {
                    var template = componentClient
                            .forEventSourcedEntity(promptTemplateName)
                            .method(PromptEntity::getPromptTemplate).invoke();
                    return new ContextForLLM(List.of(
                            new Conversation("system", template.content()),
                            new Conversation("user", cmd.exception())));
                })
                .andThen(ContextForLLM.class, response ->
                        effects()
                                .updateState(currentState().updateConversation(response))
                                .transitionTo("llm_call"));
    }

    private Step callLLM() {
        return step("llm_call")
                .call(ContextForLLM.class, context -> {
                    var s = aiService.execute(currentState().context());
                    return getChainOfThought(s.getFirst());
                }).andThen(ChainOfThought.class, this::decider);
    }

    private Step toolExecution() {
        return step("tool_execution")
                .call(ChainOfThought.class, input -> {
                    var s = toolExecutionService.execute(input, OBSERVATION_OUTPUT_FORMAT);
                    return getChainOfThought(s);
                })
                .andThen(ChainOfThought.class, this::decider);
    }

    private Effect.TransitionalEffect<Void> decider(ChainOfThought response) {
        if (response == null) {
            log.error("Unable to decide as received null response.");
            return effects().end();
        }
        // Answer found
        if (response.Answer() != null && !response.Answer().isBlank()) {
            log.info("Deciding as received an answer. {}", response.Answer());
            Conversation conversation = getConversation(response, "assistant");
            return effects()
                    .updateState(currentState().addConversation(conversation))
                    .end();
        }

        // tool calling
        if (response.Tool() != null && !response.Tool().isBlank()) {
            Conversation conversation = getConversation(response, "assistant");
            return effects()
                    .updateState(currentState().addConversation(conversation))
                    .transitionTo("tool_execution", response);
        }
        // observation
        if (response.Observation() != null && !response.Observation().isBlank()) {
            Conversation conversation = getConversation(response, "user");
            return effects()
                    .updateState(currentState().addConversation(conversation))
                    .transitionTo("llm_call", new ContextForLLM(currentState().context()));
        }
        log.warn("Unhandled response {}", response);
        return effects()
                .end();
    }

    private static Conversation getConversation(ChainOfThought response, String role) {
        ObjectMapper mapper = new ObjectMapper();
        Conversation conversation;
        try {
            String s = mapper.writeValueAsString(response);
            conversation = new Conversation(role, s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return conversation;
    }

    private ChainOfThought getChainOfThought(String content) {
        ObjectMapper mapper = new ObjectMapper();
        ChainOfThought chainOfThought = null;
        try {
            chainOfThought = mapper.readValue(content.replaceAll("\\n", ""), ChainOfThought.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert AI response to chain of thought {}", content, e);
            throw new RuntimeException(e);
        }
        return chainOfThought;
    }

}
