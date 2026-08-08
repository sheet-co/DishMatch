package dev.sheet_co.dishMatch.repository;

import dev.sheet_co.dishMatch.model.DishHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishHistoryRepository extends JpaRepository<DishHistory, Long> {
    List<DishHistory> findAllByUserIdOrderByEatenAtDesc(Long userId);
}
