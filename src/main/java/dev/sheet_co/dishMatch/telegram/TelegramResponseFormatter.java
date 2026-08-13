package dev.sheet_co.dishMatch.telegram;

import dev.sheet_co.dishMatch.model.Dish;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class TelegramResponseFormatter {

  //todo: switch types and add implementation here, so it'd be like:
  // bla bla bla. Here are your options:
  // - option 1
  // - option 2
  // !options should be tappable in telegram, so user could just press on them instead of messing with keyboard
  String format(List<Dish> chatResponse) {
    return "bla";
  }
}
