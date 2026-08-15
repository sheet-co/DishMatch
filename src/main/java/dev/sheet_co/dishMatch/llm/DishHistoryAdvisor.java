package dev.sheet_co.dishMatch.llm;

import static dev.sheet_co.dishMatch.llm.ChatClientConfiguration.USER_TELEGRAM_ID;

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
 * Appends the user's previously-eaten dish history to the outgoing prompt.
 *
 * <p>Runs after {@link org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor} (see
 * {@link #getOrder()}), and after {@link AvailableDishesAdvisor}, so the final user message reads
 * dishes-then-history — same order as the original combined template. As with {@link
 * AvailableDishesAdvisor}, this per-turn data is appended after the memory advisor already captured
 * the leaner message, so it never gets persisted to chat memory.
 */
@Component
@RequiredArgsConstructor
class DishHistoryAdvisor implements BaseAdvisor {

  final HistoryRepository historyRepository;

  @Value("classpath:/prompts/dish-recommendation-history.st")
  Resource historyPrompt;

  @Override
  public @NonNull ChatClientRequest before(
      ChatClientRequest chatClientRequest, @NonNull AdvisorChain advisorChain) {
    var telegramId = (Long) chatClientRequest.context().get(USER_TELEGRAM_ID);
    var historyText =
        historyRepository.findAllByUserTelegramId(telegramId).stream()
            .filter(history -> history.getEatenAt() != null)
            .map(Object::toString)
            .collect(Collectors.joining("\n"));

    var block = new PromptTemplate(historyPrompt).render(Map.of("history", historyText));
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
    return DishHistoryAdvisor.class.getSimpleName();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 260;
  }
}
