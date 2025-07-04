package com.akka.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import com.akka.application.PromptEntity;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/api/prompts")
public class PromptEndpoint {

    private final ComponentClient componentClient;

    public PromptEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Get("/{promptName}")
    public PromptEntity.PromptTemplate getPromptByName(String promptName) {
        return componentClient.forEventSourcedEntity(promptName)
                .method(PromptEntity::getPromptTemplate)
                .invoke();
    }

    @Post()
    public PromptEntity.PromptTemplate addPrompt(PromptEntity.PromptTemplate request) {
        return componentClient
                .forEventSourcedEntity(request.name())
                .method(PromptEntity::addPromptTemplate)
                .invoke(request);
    }
}
