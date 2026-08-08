package dev.sheet_co.dishMatch.configuration;

import dev.sheet_co.dishMatch.PromptUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfiguration {
    @Bean
    public ChatClient initGeminiClient(ChatModel model) {
        return ChatClient.builder(model)
                         .defaultSystem(PromptUtils.startPrompt)
                         .defaultAdvisors(new SimpleLoggerAdvisor())
                         .build();
    }
}
