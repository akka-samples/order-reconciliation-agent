package com.akka.application.state;


import com.akka.domain.Conversation;

import java.util.List;

public record ContextForLLM(List<Conversation> conversationList) {
}
