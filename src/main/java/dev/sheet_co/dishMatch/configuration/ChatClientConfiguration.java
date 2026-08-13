package dev.sheet_co.dishMatch.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfiguration {

  @Value("classpath:/prompts/dish-recommendation-system.st")
  private Resource dishRecommendationSystemPrompt;

  @Bean
  public ChatClient initGeminiClient(ChatModel model) {
    return ChatClient.builder(model)
        .defaultSystem(dishRecommendationSystemPrompt)
        .defaultAdvisors(new SimpleLoggerAdvisor())
        .build();
  }
}
