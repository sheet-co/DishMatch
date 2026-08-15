package dev.sheet_co.dishMatch.llm;

import static dev.sheet_co.dishMatch.llm.ChatClientConfiguration.USER_TELEGRAM_ID;

import dev.sheet_co.dishMatch.dto.ChatRequest;
import dev.sheet_co.dishMatch.model.Dish;
import dev.sheet_co.dishMatch.model.History;
import dev.sheet_co.dishMatch.repository.HistoryRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatClient chatClient;
  private final HistoryRepository historyRepository;

  @Value("classpath:/prompts/dish-recommendation-preference.st")
  private Resource dishRecommendationPreferencePrompt;

  public List<Dish> recommend(ChatRequest request) {
    var candidateCount = historyRepository.countByUserTelegramId(request.userId());
    if (candidateCount <= 1) {
      return historyRepository.findAllByUserTelegramId(request.userId()).stream()
          .map(History::getDish)
          .toList();
    }

    var userPreference =
        Optional.ofNullable(request.message())
            .filter(s -> !s.isBlank())
            .orElse("Особых пожеланий нет");

    var userMessage =
        new PromptTemplate(dishRecommendationPreferencePrompt)
            .createMessage(Map.of("preference", userPreference));

    List<Long> dishIds =
        chatClient
            .prompt()
            .messages(userMessage)
            .advisors(
                a ->
                    a.param(ChatMemory.CONVERSATION_ID, String.valueOf(request.chatId()))
                        .param(USER_TELEGRAM_ID, request.userId()))
            .call()
            .entity(
                new ParameterizedTypeReference<>() {},
                spec -> spec.useProviderStructuredOutput().validateSchema());

    if (dishIds == null || dishIds.isEmpty()) {
      return List.of();
    }

    return historyRepository.findAllByUserTelegramId(request.userId()).stream()
        .map(History::getDish)
        .filter(dish -> dishIds.contains(dish.getId()))
        .toList();
  }
}
