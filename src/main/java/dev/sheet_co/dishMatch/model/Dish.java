package dev.sheet_co.dishMatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "dishes")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Dish {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "ingredients", columnDefinition = "text[]")
  private List<String> ingredients;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "tags", columnDefinition = "text[]")
  private List<String> tags;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  @OneToMany(mappedBy = "dish", fetch = FetchType.LAZY)
  private List<History> history = new ArrayList<>();

  /**
   * Overrides Lombok's generated setter: no code path can put un-normalised ingredients on a {@code
   * Dish} instance. Primary normalisation happens here; {@link #normaliseBeforeSave()} is only a
   * backstop for anything that mutates the fields without going through this setter.
   */
  public void setIngredients(List<String> ingredients) {
    this.ingredients = TagNormalizer.normalise(ingredients);
  }

  /** Same guarantee as {@link #setIngredients(List)}, for tags. */
  public void setTags(List<String> tags) {
    this.tags = TagNormalizer.normalise(tags);
  }

  /**
   * Backstop only — re-normalises on flush in case something bypassed the setters (e.g. Hibernate
   * hydrating this entity from a row that predates the normaliser). Not the primary guarantee: this
   * fires at flush time, so code reading the fields earlier in the same transaction won't see it
   * retroactively.
   */
  @PrePersist
  @PreUpdate
  private void normaliseBeforeSave() {
    ingredients = TagNormalizer.normalise(ingredients);
    tags = TagNormalizer.normalise(tags);
  }

  @Override
  public String toString() {
    return "id: %d, name: %s, ingridients: %s, tags: %s".formatted(id, name, ingredients, tags);
  }
}
