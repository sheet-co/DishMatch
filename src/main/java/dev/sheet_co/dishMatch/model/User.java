package dev.sheet_co.dishMatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

  @Id
  @Column(name = "telegram_id")
  private Long telegramId;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<History> history = new ArrayList<>();
}
