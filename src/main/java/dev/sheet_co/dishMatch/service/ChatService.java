package dev.sheet_co.dishMatch.service;

import dev.sheet_co.dishMatch.model.Dish;
import dev.sheet_co.dishMatch.model.History;
import dev.sheet_co.dishMatch.repository.HistoryRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
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

  @Value("classpath:/prompts/dish-recommendation-user.st")
  private Resource dishRecommendationUserPrompt;

  public List<Dish> reccomend(String preference, Long userId) {
    var userHistory = historyRepository.findAllByUserTelegramId(userId);
    var usersDishes = userHistory.stream().map(History::getDish).toList();

    if (usersDishes.isEmpty()) {
      return List.of();
    }
    if (usersDishes.size() == 1) {
      return usersDishes;
    }

    var dishesText = usersDishes.stream().map(Object::toString).collect(Collectors.joining("\n"));
    var historyText =
        userHistory.stream()
            .filter(dish -> dish.getEatenAt() != null)
            .map(Object::toString)
            .collect(Collectors.joining("\n"));

    String userPreference =
        preference == null || preference.isBlank() ? "Особых пожеланий нет" : preference;

    var userMessage =
        new PromptTemplate(dishRecommendationUserPrompt)
            .createMessage(
                Map.of(
                    "preference", userPreference,
                    "dishes", dishesText,
                    "history", historyText
                ));
    List<Long> dishIds =
        chatClient
            .prompt()
            .messages(userMessage)
            .call()
            .entity(
                new ParameterizedTypeReference<>() {
                },
                spec -> spec.useProviderStructuredOutput().validateSchema()
            );

    if (dishIds == null || dishIds.isEmpty()) {
      return List.of();
    }

    return usersDishes.stream().filter(dish -> dishIds.contains(dish.getId())).toList();
  }
}
