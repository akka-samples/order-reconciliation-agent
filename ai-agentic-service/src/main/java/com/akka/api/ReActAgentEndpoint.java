package com.akka.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.akka.application.ReActWorkflow;
import com.akka.application.command.ProcessIncident;
import com.akka.domain.ExceptionReasoningRequest;
import com.akka.domain.ExceptionReasoningResponse;
import com.akka.domain.GetConversations;

import java.util.UUID;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/api/react-agent")
public class ReActAgentEndpoint {

    private final ComponentClient componentClient;
    private final String queryTemplate = """
            Question:
                The event failed for following reasons. I want to retry the request by solving the following issues.
                Help me solving this.
                %s
            """;

    public ReActAgentEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Get("/{agentId}")
    public GetConversations getConversationsForAgent(String agentId) {
        return new GetConversations(componentClient
                .forWorkflow(agentId)
                .method(ReActWorkflow::getConversations)
                .invoke());
    }

    @Post()
    public ExceptionReasoningResponse requestAgentToSolveIncident(ExceptionReasoningRequest request) {
        String agentId = UUID.randomUUID().toString();
        ProcessIncident command = new ProcessIncident(agentId, String.format(queryTemplate, request.exceptionDetails()));
        return new ExceptionReasoningResponse(componentClient
                .forWorkflow(agentId)
                .method(ReActWorkflow::solveIncident)
                .invoke(command));
    }
}
