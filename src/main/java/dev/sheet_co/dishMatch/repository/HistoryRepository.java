package dev.sheet_co.dishMatch.repository;

import dev.sheet_co.dishMatch.model.History;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

  @Query(
      """
          select h
          from History h
          join fetch h.dish
          where h.user.telegramId = :telegramId
          """)
  List<History> findAllByUserTelegramId(Long telegramId);

  long countByUserTelegramId(Long telegramId);
}
