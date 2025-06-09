package com.akka.domain;

import java.util.Objects;

public  class Conversation {
    private String role;
    private String content;

    public Conversation() {
    }

    public Conversation(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String role() {
        return role;
    }

    public String content() {
        return content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Conversation) obj;
        return Objects.equals(this.role, that.role) &&
                Objects.equals(this.content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content);
    }

    @Override
    public String toString() {
        return "Conversation[" +
                "role=" + role + ", " +
                "content=" + content + ']';
    }

}
