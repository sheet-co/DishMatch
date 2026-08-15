package dev.sheet_co.dishMatch;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
@ComponentScan(
    basePackages = "dev.sheet_co.dishMatch",
    excludeFilters = {
      // Skip the telegram module: it needs a real bot token and starts a long-polling
      // client we don't want running in this test.
      @ComponentScan.Filter(
          type = FilterType.REGEX,
          pattern = "dev\\.sheet_co\\.dishMatch\\.telegram\\..*"),
      // Skip the real @SpringBootApplication class: scanning it back in would register
      // its @EnableJpaAuditing a second time and collide with the one above.
      @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = DishMatchApplication.class)
    })
public class NoTelegramTestConfig {}
