package dev.sheet_co.dishMatch.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfiguration {
  private static final String START_PROMPT =
      """
        Ты система рекомендации блюд. Пользователь передаёт тебе:
        1. Пожелание по еде.
        2. Список доступных ему блюд.
        3. Историю ранее съеденных блюд.
        Выбери 1 или 2 наиболее подходящих блюда.
        ПРАВИЛА:
        - выбирай ТОЛЬКО из списка доступных блюд;
        - возвращай только существующие dishId;
        - никогда не придумывай новое блюдо;
        - учитывай название, ингредиенты и теги блюда;
        - максимально учитывай пожелание пользователя;
        - обязательно учитывай историю питания;
        - если блюдо пользователь ел недавно,
          его приоритет должен быть ниже;
        - если блюдо пользователь ел несколько
          раз за последнее время,
          его приоритет должен быть ещё ниже;
        - блюда, которые давно не ели
          или никогда не ели,
          имеют более высокий приоритет;
        - верни минимум 1 и максимум 2 dishId.
        """;

  @Bean
  public ChatClient initGeminiClient(ChatModel model) {
    return ChatClient.builder(model)
        .defaultSystem(START_PROMPT)
        .defaultAdvisors(new SimpleLoggerAdvisor())
        .build();
  }
}
