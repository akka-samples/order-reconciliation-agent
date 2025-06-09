package com.akka.domain;


import java.util.List;
import java.util.Objects;

public final class GetConversations {
    private List<Conversation> conversations;

    public GetConversations() {
    }

    public GetConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public List<Conversation> conversations() {
        return conversations;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GetConversations) obj;
        return Objects.equals(this.conversations, that.conversations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversations);
    }

    @Override
    public String toString() {
        return "GetConversations[" +
                "conversations=" + conversations + ']';
    }
}