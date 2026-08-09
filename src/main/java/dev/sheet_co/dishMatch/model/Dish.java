package dev.sheet_co.dishMatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
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

  private Long userId;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "tags", columnDefinition = "text[]")
  private List<String> tags;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  @Override
  public String toString() {
    return "id: %d, name: %s, ingridients: %s, tags: %s".formatted(id, name, ingredients, tags);
  }
}
