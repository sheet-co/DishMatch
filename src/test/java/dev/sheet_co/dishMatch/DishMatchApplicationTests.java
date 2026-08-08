package dev.sheet_co.dishMatch;

import dev.sheet_co.dishMatch.model.Dish;
import dev.sheet_co.dishMatch.model.DishHistory;
import dev.sheet_co.dishMatch.repository.DishHistoryRepository;
import dev.sheet_co.dishMatch.repository.DishRepository;
import dev.sheet_co.dishMatch.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Testcontainers
class DishMatchApplicationTests {
	private static final Long USER_ID = 100L;
	private static final Long OTHER_USER_ID = 200L;
	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres =
		new PostgreSQLContainer(
			"postgres:16-alpine"
		);

	@Autowired
	private DishRepository dishRepository;


	@Autowired
	private DishHistoryRepository dishHistoryRepository;


	@Autowired
	private ChatService chatService;


	private Dish plov;
	private Dish carbonara;
	private Dish steak;
	private Dish salad;
	private Dish chickenPasta;
	private Dish otherUserDish;


	@BeforeEach
	void setUp() {
		dishHistoryRepository.deleteAll();
		dishRepository.deleteAll();

		plov = createDish(
        USER_ID,
        "Плов",

        List.of(
				"рис",
				"говядина",
				"морковь",
				"лук"
			),

        List.of(
				"мясное",
				"сытное",
				"долго"
			)
		);

		carbonara = createDish(
			USER_ID,
			"Карбонара",

			List.of(
				"спагетти",
				"бекон",
				"яйца",
				"пармезан"
			),

			List.of(
				"мясное",
				"сытное",
				"быстро"
			)
		);

		steak = createDish(
			USER_ID,
			"Стейк",

			List.of(
				"говядина",
				"сливочное масло",
				"чеснок"
			),

			List.of(
				"мясное",
				"сытное",
				"белковое"
			)
		);

		salad = createDish(
			USER_ID,
			"Овощной салат",

			List.of(
				"помидоры",
				"огурцы",
				"зелень"
			),

			List.of(
				"лёгкое",
				"овощное",
				"быстро"
			)
		);

		chickenPasta = createDish(
			USER_ID,
			"Паста с курицей",

			List.of(
				"паста",
				"курица",
				"сливки",
				"сыр"
			),

			List.of(
				"мясное",
				"сытное",
				"быстро"
			)
		);


		otherUserDish =
			createDish(
				OTHER_USER_ID,
				"Суши",

				List.of(
					"рис",
					"лосось",
					"нори"
				),

				List.of(
					"рыба",
					"японское"
				)
			);

		createHistory(
        plov,
        Instant.now()
               .minus(
                   1,
                   ChronoUnit.DAYS
			       )
		);

		createHistory(
			plov,
			Instant.now()
			       .minus(
				       5,
				       ChronoUnit.DAYS
			       )
		);

		createHistory(
			carbonara,
			Instant.now()
			       .minus(
				       25,
				       ChronoUnit.DAYS
			       )
		);

	}


	@Test
	void shouldRecommendDishUsingRealDatabaseAndRealGemini() {

		List<Dish> databaseDishes =
			dishRepository.findAllByUserId(USER_ID);


		assertThat(databaseDishes)
			.hasSize(5);


		List<DishHistory> history =
			dishHistoryRepository
				.findAllByUserIdOrderByEatenAtDesc(
					USER_ID
				);

		assertThat(history)
			.hasSize(3);


		/*
		 * =================================================
		 * НАСТОЯЩИЙ ЗАПРОС GEMINI
		 * =================================================
		 */

		List<Dish> result =
			chatService.reccomend(
				"""
        Хочу что-нибудь мясное,
        сытное и желательно быстрое
        """,
				USER_ID
			);

		assertThat(result)
			.isNotEmpty();

		assertThat(result.size())
			.isBetween(1, 2);

		assertThat(result)
			.allMatch(
				dish ->
					USER_ID.equals(
						dish.getUserId()
					)
			);

		assertThat(result)
			.extracting(Dish::getId)
			.doesNotContain(
				otherUserDish.getId()
			);

		List<Long> availableDishIds =
			databaseDishes.stream()
			              .map(Dish::getId)
			              .toList();


		assertThat(result)
			.extracting(Dish::getId)
			.allMatch(
				availableDishIds::contains
			);

		System.out.println();
		System.out.println(
			"======================================"
		);

		System.out.println(
			"        REAL GEMINI RESULT"
		);

		System.out.println(
			"======================================"
		);


		result.forEach(dish -> {

			System.out.println(
				"ID: " + dish.getId()
			);

			System.out.println(
				"NAME: " + dish.getName()
			);

			System.out.println(
				"INGREDIENTS: "
					+ dish.getIngredients()
			);

			System.out.println(
				"TAGS: "
					+ dish.getTags()
			);

			System.out.println(
				"--------------------------------------"
			);
		});
	}

	private Dish createDish(
		Long userId,
		String name,
		List<String> ingredients,
		List<String> tags
	) {

		Dish dish = new Dish();

		dish.setUserId(userId);
		dish.setName(name);
		dish.setIngredients(ingredients);
		dish.setTags(tags);

		return dishRepository.save(dish);
	}

	private DishHistory createHistory(
		Dish dish,
		Instant eatenAt
	) {

		DishHistory history =
			new DishHistory();

		history.setUserId(USER_ID);

		history.setDish(dish);

		history.setEatenAt(eatenAt);

		return dishHistoryRepository.save(history);
	}
}

