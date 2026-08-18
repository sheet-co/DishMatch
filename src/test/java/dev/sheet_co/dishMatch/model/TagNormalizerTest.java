package dev.sheet_co.dishMatch.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class TagNormalizerTest {

  @Test
  void trimsWhitespace() {
    assertThat(TagNormalizer.normalise(List.of("  chicken ", "mayo\t")))
        .containsExactly("chicken", "mayo");
  }

  @Test
  void dropsBlankAndWhitespaceOnlyValues() {
    assertThat(TagNormalizer.normalise(Arrays.asList("chicken", "   ", "", "mayo")))
        .containsExactly("chicken", "mayo");
  }

  @Test
  void dropsNullElements() {
    assertThat(TagNormalizer.normalise(Arrays.asList("chicken", null, "mayo")))
        .containsExactly("chicken", "mayo");
  }

  @Test
  void lowerCasesWithFixedLocale() {
    assertThat(TagNormalizer.normalise(List.of("Chicken", "MAYO")))
        .containsExactly("chicken", "mayo");
  }

  @Test
  void deduplicatesCaseInsensitively() {
    assertThat(TagNormalizer.normalise(List.of("Chicken", "chicken", " chicken ")))
        .containsExactly("chicken");
  }

  @Test
  void sortsResultForStableOrdering() {
    assertThat(TagNormalizer.normalise(List.of("sweet", "breakfast", "chicken", "mayo")))
        .containsExactly("breakfast", "chicken", "mayo", "sweet");
  }

  @Test
  void returnsEmptyListForNullInput() {
    assertThat(TagNormalizer.normalise(null)).isEmpty();
  }

  @Test
  void isIdempotent() {
    List<String> once = TagNormalizer.normalise(List.of(" Chicken", "MAYO ", "chicken"));
    List<String> twice = TagNormalizer.normalise(once);

    assertThat(twice).isEqualTo(once);
  }

  @Test
  void returnsImmutableList() {
    List<String> result = TagNormalizer.normalise(List.of("chicken"));

    assertThat(result).isUnmodifiable();
  }
}
