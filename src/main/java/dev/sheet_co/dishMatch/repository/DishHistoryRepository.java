package dev.sheet_co.dishMatch.repository;

import dev.sheet_co.dishMatch.model.DishHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DishHistoryRepository extends JpaRepository<DishHistory, Long> {
  List<DishHistory> findAllByUserIdOrderByEatenAtDesc(Long userId);
}
