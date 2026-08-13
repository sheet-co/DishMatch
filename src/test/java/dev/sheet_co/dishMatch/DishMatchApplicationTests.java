package dev.sheet_co.dishMatch;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import dev.sheet_co.dishMatch.dto.ChatRequest;
import dev.sheet_co.dishMatch.model.Dish;
import dev.sheet_co.dishMatch.model.History;
import dev.sheet_co.dishMatch.model.User;
import dev.sheet_co.dishMatch.repository.DishRepository;
import dev.sheet_co.dishMatch.repository.HistoryRepository;
import dev.sheet_co.dishMatch.repository.UserRepository;
import dev.sheet_co.dishMatch.service.ChatService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@Slf4j
class DishMatchApplicationTests {

  private static final Long USER_ID = 100L;
  private static final Long OTHER_USER_ID = 200L;

  @Container
  @ServiceConnection
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

  @Autowired
  private DishRepository dishRepository;

  @Autowired
  private HistoryRepository historyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChatService chatService;

  private User user;
  private User otherUser;

  private Dish plov;
  private Dish carbonara;
  private Dish steak;
  private Dish salad;
  private Dish chickenPasta;
  private Dish sushi;

  @BeforeEach
  void setUp() {

    historyRepository.deleteAll();
    dishRepository.deleteAll();
    userRepository.deleteAll();

    user = createUser(USER_ID);
    otherUser = createUser(OTHER_USER_ID);

    plov =
        createDish(
            "Плов",
            List.of("рис", "говядина", "морковь", "лук"),
            List.of("мясное", "сытное", "долго"));

    carbonara =
        createDish(
            "Карбонара",
            List.of("спагетти", "бекон", "яйца", "пармезан"),
            List.of("мясное", "сытное", "быстро"));

    steak =
        createDish(
            "Стейк",
            List.of("говядина", "сливочное масло", "чеснок"),
            List.of("мясное", "сытное", "белковое"));

    salad =
        createDish(
            "Овощной салат",
            List.of("помидоры", "огурцы", "зелень"),
            List.of("лёгкое", "овощное", "быстро"));

    chickenPasta =
        createDish(
            "Паста с курицей",
            List.of("паста", "курица", "сливки", "сыр"),
            List.of("мясное", "сытное", "быстро"));

    sushi = createDish("Суши", List.of("рис", "лосось", "нори"), List.of("рыба", "японское"));

    // Пользователь ел плов недавно
    createHistory(user, plov, Instant.now().minus(1, ChronoUnit.DAYS));

    // Карбонару ел давно
    createHistory(user, carbonara, Instant.now().minus(25, ChronoUnit.DAYS));

    // Эти блюда пользователь добавил,
    // но ещё не ел
    createHistory(user, steak, null);
    createHistory(user, salad, null);
    createHistory(user, chickenPasta, null);

    // Суши принадлежат списку другого пользователя
    createHistory(otherUser, sushi, null);
  }

  @Test
  void shouldRecommendDishUsingRealDatabaseAndRealGemini() {

    List<History> userHistory = historyRepository.findAllByUserTelegramId(USER_ID);

    // У пользователя в списке 5 блюд
    assertThat(userHistory).hasSize(5);

    List<Dish> userDishes = userHistory.stream().map(History::getDish).toList();

    assertThat(userDishes).hasSize(5);

    assertThat(userDishes)
        .extracting(Dish::getId)
        .containsExactlyInAnyOrder(
            plov.getId(), carbonara.getId(), steak.getId(), salad.getId(), chickenPasta.getId());

    var request = new ChatRequest(
        "Хочу что-нибудь мясное, сытное и желательно быстрое", USER_ID, 1L, "Bobby"
    );
    List<Dish> result =
        chatService.recommend(request);

    assertThat(result).isNotEmpty();

    assertThat(result.size()).isBetween(1, 2);

    // Чужое блюдо не должно попасть в рекомендации
    assertThat(result).extracting(Dish::getId).doesNotContain(sushi.getId());

    List<Long> userDishIds = userDishes.stream().map(Dish::getId).toList();

    // Gemini может вернуть только блюдо из списка пользователя
    assertThat(result).extracting(Dish::getId).allMatch(userDishIds::contains);

    log.info("GEMINI RESULT");

    result.forEach(
        dish -> {
          log.info("ID: {}", dish.getId());
          log.info("NAME: {}", dish.getName());
          log.info("INGREDIENTS: {}", dish.getIngredients());
          log.info("TAGS: {}", dish.getTags());
        });
  }

  private User createUser(Long telegramId) {

    User localUser = new User();
    localUser.setTelegramId(telegramId);

    return userRepository.save(localUser);
  }

  private Dish createDish(String name, List<String> ingredients, List<String> tags) {

    Dish dish = new Dish();

    dish.setName(name);
    dish.setIngredients(ingredients);
    dish.setTags(tags);

    return dishRepository.save(dish);
  }

  private History createHistory(User user, Dish dish, Instant eatenAt) {

    History history = new History();

    history.setUser(user);
    history.setDish(dish);
    history.setEatenAt(eatenAt);

    return historyRepository.save(history);
  }
}
