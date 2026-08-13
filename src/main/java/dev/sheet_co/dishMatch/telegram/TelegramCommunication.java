package dev.sheet_co.dishMatch.telegram;

import dev.sheet_co.dishMatch.dto.ChatRequest;
import dev.sheet_co.dishMatch.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Dummy long-polling bot: echoes back any text message it receives. Registered automatically by
 * {@code telegrambots-springboot-longpolling-starter} because it implements
 * {@link SpringLongPollingBot}. Extends {@link DefaultLongPollingUpdateConsumer}, which dispatches
 * each update to {@link #consume(Update)} on a worker thread pool.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class TelegramCommunication implements LongPollingUpdateConsumer {

  final TelegramClient client;
  final ChatService chatService;
  final TelegramResponseFormatter responseFormatter;
  final VirtualThreadTaskExecutor executor;

  void consume(Update update) {
    if (!update.hasMessage() || !update.getMessage().hasText()) {
      return;
    }

    var message = update.getMessage();
    var request = new ChatRequest(
        message.getText(),
        message.getFrom().getId(),
        message.getChatId(),
        message.getFrom().getUserName()
    );
    log.info("Received message {}", request);

    var chatResponse = chatService.recommend(request);
    var formattedResponse = responseFormatter.format(chatResponse);

    var reply =
        SendMessage.builder().chatId(request.chatId()).text(formattedResponse).build();

    try {
      client.execute(reply);
    } catch (TelegramApiException e) {
      log.error("Failed to send message to chat {}", request.chatId(), e);
    }
  }

  @Override
  public void consume(List<Update> updates) {
    for (var update : updates) {
      executor.execute(() -> consume(update));
    }
  }
}
