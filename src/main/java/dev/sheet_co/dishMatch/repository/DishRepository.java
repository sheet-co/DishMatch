package dev.sheet_co.dishMatch.repository;

import dev.sheet_co.dishMatch.model.Dish;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
  List<Dish> findAllByUserId(Long userId);
}
