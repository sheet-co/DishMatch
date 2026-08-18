package dev.sheet_co.dishMatch.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Canonicalises free-form string collections (dish tags, ingredients) before they're persisted.
 *
 * <p>Pure and idempotent on purpose: trims whitespace, drops blanks, lower-cases with a fixed
 * locale, de-duplicates and sorts. Safe to call more than once on the same value, and safe to call
 * outside a Spring context (no dependencies), which keeps it trivially unit-testable.
 */
final class TagNormalizer {

  private TagNormalizer() {}

  public static List<String> normalise(List<String> values) {

    if (values == null) {
      return List.of();
    }

    return values.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .map(value -> value.toLowerCase(Locale.ROOT))
        .distinct()
        .sorted()
        .toList();
  }
}
