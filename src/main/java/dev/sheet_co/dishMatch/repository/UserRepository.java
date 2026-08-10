package dev.sheet_co.dishMatch.repository;

import dev.sheet_co.dishMatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {}
