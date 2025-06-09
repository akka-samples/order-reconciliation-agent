package com.akka.service;

import com.akka.domain.Conversation;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.ChatModel;
import com.openai.models.ResponseFormatJsonSchema;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final OpenAIClient client;

    public AiService() {
        this.client = OpenAIOkHttpClient.fromEnv();
    }

    public List<String> execute(List<Conversation> context) {
        ChatCompletionCreateParams createParams = this.getChatCompletionCreateParams(context);
        return client.chat().completions().create(createParams).choices().stream()
                .flatMap(choice -> choice.message().content().stream())
                .toList();
    }

    private final ResponseFormatJsonSchema.JsonSchema.Schema schema = ResponseFormatJsonSchema.JsonSchema.Schema.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty(
                    "properties", JsonValue.from(Map.of(
                            "Thought", Map.of("type", "string"),
                            "Tool", Map.of("type", "string"),
                            "PAUSE", Map.of("type", "boolean"),
                            "input", Map.of("type", "string"),
                            "Observation", Map.of("type", "string"),
                            "Answer", Map.of("type", "string")
                    ))).build();

    private ChatCompletionCreateParams getChatCompletionCreateParams(List<Conversation> context) {
        ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O)
                .maxCompletionTokens(2048)
                .responseFormat(ResponseFormatJsonSchema.builder()
                        .jsonSchema(ResponseFormatJsonSchema.JsonSchema.builder()
                                .name("cot")
                                .schema(schema)
                                .build())
                        .build());
        for (Conversation conversation : context) {
            if (conversation.role().equalsIgnoreCase("user")) {
                builder.addUserMessage(conversation.content());
            } else if (conversation.role().equalsIgnoreCase("assistant")) {
                builder.addMessage(
                        ChatCompletionAssistantMessageParam
                                .builder()
                                .role(JsonValue.from(conversation.role()))
                                .content(ChatCompletionAssistantMessageParam.Content.ofText(conversation.content())).build());
            } else {
                builder.addSystemMessage(conversation.content());
            }
        }
        return builder.build();
    }
}
