package dev.sheet_co.dishMatch.service;

import dev.sheet_co.dishMatch.PromptUtils;
import dev.sheet_co.dishMatch.model.Dish;
import dev.sheet_co.dishMatch.model.DishHistory;
import dev.sheet_co.dishMatch.repository.DishHistoryRepository;
import dev.sheet_co.dishMatch.repository.DishRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatClient chatClient;
    private final DishRepository dishRepository;
    private final DishHistoryRepository dishHistoryRepository;

    @Transactional(readOnly = true)
    public List<Dish> reccomend(String preference, Long userId) {
        List<Dish> dishes = dishRepository.findAllByUserId(userId);
        if (dishes.isEmpty()) {
            return List.of();
        } else if (dishes.size() == 1) {
            return dishes;
        } else {
            List<DishHistory> dishesHistory = dishHistoryRepository.findAllByUserIdOrderByEatenAtDesc(userId);

            String dishesText = dishes.stream()
                                      .map(dish -> """
                                          id: %d
                                          name: %s
                                          ingridients: %s
                                          tags: %s 
                                          """.formatted(
                                          dish.getId(),
                                          dish.getName(),
                                          dish.getIngredients(),
                                          dish.getTags()
                                      )).collect(Collectors.joining("\n"));

            String historyText = dishesHistory.stream()
                                              .map(dish -> """
                                                  dishId: %d
                                                  eatenAt: %s 
                                                  """.formatted(
                                                  dish.getId(),
                                                  dish.getEatenAt()
                                              ))
                                              .collect(Collectors.joining("\n"));

            String userPreference = preference == null || preference.isBlank()
                                    ? "Особых пожеланий нет"
                                    : preference;

            List<Long> dishIds =
                chatClient.prompt()
                          .user(PromptUtils.createUserPrompt(userPreference, dishesText, historyText))
                          .call()
                          .entity(
                              new ParameterizedTypeReference<List<Long>>() {
                              }, spec -> spec
                                  .useProviderStructuredOutput()
                                  .validateSchema()
                          );

            return dishes.stream().filter(dish -> dishIds.contains(dish.getId())).toList();

        }
    }

}
