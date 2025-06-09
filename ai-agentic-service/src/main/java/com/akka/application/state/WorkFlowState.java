package com.akka.application.state;

import com.akka.domain.Conversation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record WorkFlowState(List<Conversation> context, AgentStatus status) {
    public enum AgentStatus {
        INITIATED, CONVERSATION_STATE, COMPLETED
    }

    public WorkFlowState updateConversation(ContextForLLM conversation) {
        List<Conversation> newList = Stream.concat(context.stream(), conversation.conversationList().stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), List::copyOf));
        return new WorkFlowState(newList, status);
    }

    public WorkFlowState addConversation(Conversation conversation, AgentStatus status) {
        context.add(conversation);
        return new WorkFlowState(context, status);
    }

    public WorkFlowState addConversation(Conversation conversation) {
        context.add(conversation);
        return new WorkFlowState(context, status);
    }

}
