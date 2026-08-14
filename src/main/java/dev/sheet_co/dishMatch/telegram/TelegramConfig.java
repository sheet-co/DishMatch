package dev.sheet_co.dishMatch.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
class TelegramConfig {

  @Bean
  TelegramClient telegramClient(@Value("${telegram.bot.token}") String botToken) {
    return new OkHttpTelegramClient(botToken);
  }

  @Bean
  VirtualThreadTaskExecutor telegramExecutor() {
    return new VirtualThreadTaskExecutor("tg-");
  }
}

@Component
@RequiredArgsConstructor
class TelegramRegistration implements SpringLongPollingBot {

  @Value("${telegram.bot.token}")
  private String botToken;

  final TelegramCommunication telegramCommunication;

  @Override
  public String getBotToken() {
    return botToken;
  }

  @Override
  public LongPollingUpdateConsumer getUpdatesConsumer() {
    return telegramCommunication;
  }
}
