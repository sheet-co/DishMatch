package dev.sheet_co.dishMatch.service;

import dev.sheet_co.dishMatch.model.Dish;
import dev.sheet_co.dishMatch.repository.HistoryRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatClient chatClient;
  private final HistoryRepository historyRepository;

  public List<Dish> reccomend(String preference, Long userId) {
    var userHistory = historyRepository.findAllByUserTelegramId(userId);
    var usersDishes = userHistory.stream().map(history -> history.getDish()).toList();

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

    var promptText =
        new PromptTemplate(
            """
                Пожелание пользователя: {preference}
                Доступные блюда: {dishes}
                История пользователя: {history}
                Выбери 1 или 2 наиболее подходящих блюда.
                Учитывай пожелание пользователя.
                Учитывай историю питания:
                если блюдо пользователь ел недавно,
                его приоритет должен быть ниже.
                
                Если блюдо давно не ели
                или никогда не ели,
                его приоритет должен быть выше.
                Верни только JSON-массив dishId.
                Пример:
                [3, 7]
                """)
            .render(
                Map.of(
                    "preference", userPreference,
                    "dishes", dishesText,
                    "history", historyText
                ));
    List<Long> dishIds =
        chatClient
            .prompt()
            .user(promptText)
            .call()
            .entity(
                new ParameterizedTypeReference<List<Long>>() {
                },
                spec -> spec.useProviderStructuredOutput().validateSchema()
            );

    return usersDishes.stream().filter(dish -> dishIds.contains(dish.getId())).toList();
  }
}
