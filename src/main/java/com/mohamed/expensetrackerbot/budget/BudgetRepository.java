package com.mohamed.expensetrackerbot.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget,Long> {


    Optional<Budget> findByUserIdAndMonthAndYear(Long userId, int month, int year);
}
