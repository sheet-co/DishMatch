package dev.sheet_co.dishMatch.llm;

import static dev.sheet_co.dishMatch.llm.ChatClientConfiguration.USER_TELEGRAM_ID;

import dev.sheet_co.dishMatch.model.History;
import dev.sheet_co.dishMatch.repository.HistoryRepository;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Appends the user's available dishes (all of their {@link History} rows, mapped to {@link
 * dev.sheet_co.dishMatch.model.Dish}) to the outgoing prompt.
 *
 * <p>Runs after {@link org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor} (see
 * {@link #getOrder()}), so the fetched dish list is only ever seen by the model — the memory
 * advisor has already captured the leaner message before this advisor appends to it, so none of
 * this per-turn data gets persisted to {@link org.springframework.ai.chat.memory.ChatMemory}.
 */
@Component
@RequiredArgsConstructor
class AvailableDishesAdvisor implements BaseAdvisor {

  final HistoryRepository historyRepository;

  @Value("classpath:/prompts/dish-recommendation-dishes.st")
  Resource dishesPrompt;

  @Override
  public @NonNull ChatClientRequest before(
      ChatClientRequest chatClientRequest, @NonNull AdvisorChain advisorChain) {
    var telegramId = (Long) chatClientRequest.context().get(USER_TELEGRAM_ID);
    var dishesText =
        historyRepository.findAllByUserTelegramId(telegramId).stream()
            .map(History::getDish)
            .map(Object::toString)
            .collect(Collectors.joining("\n"));

    var block = new PromptTemplate(dishesPrompt).render(Map.of("dishes", dishesText));
    var augmentedPrompt =
        chatClientRequest
            .prompt()
            .augmentUserMessage(
                userMessage ->
                    userMessage.mutate().text(userMessage.getText() + "\n" + block).build());

    return chatClientRequest.mutate().prompt(augmentedPrompt).build();
  }

  @Override
  public @NonNull ChatClientResponse after(
      @NonNull ChatClientResponse chatClientResponse, @NonNull AdvisorChain advisorChain) {
    return chatClientResponse;
  }

  @Override
  public @NonNull String getName() {
    return AvailableDishesAdvisor.class.getSimpleName();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 250;
  }
}
