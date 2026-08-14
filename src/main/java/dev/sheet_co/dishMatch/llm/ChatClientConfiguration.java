package dev.sheet_co.dishMatch.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
class ChatClientConfiguration {

  /** How many past messages to keep per Telegram user before older ones get evicted. */
  private static final int CHAT_MEMORY_MAX_MESSAGES = 30;

  /** Advisor-context key the caller must set via {@code .advisors(a -> a.param(...))}. */
  static final String USER_TELEGRAM_ID = "userTelegramId";

  @Value("classpath:/prompts/dish-recommendation-system.st")
  Resource dishRecommendationSystemPrompt;

  @Bean
  ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(chatMemoryRepository)
        .maxMessages(CHAT_MEMORY_MAX_MESSAGES)
        .build();
  }

  @Bean
  ChatClient initGeminiClient(
      ChatModel model,
      ChatMemory chatMemory,
      AvailableDishesAdvisor availableDishesAdvisor,
      DishHistoryAdvisor dishHistoryAdvisor) {
    return ChatClient.builder(model)
        .defaultSystem(dishRecommendationSystemPrompt)
        .defaultAdvisors(
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
            availableDishesAdvisor,
            dishHistoryAdvisor,
            new SimpleLoggerAdvisor())
        .build();
  }
}
