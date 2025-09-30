package com.siddesh.expensetracker.repository;

import com.siddesh.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // This method will find all expenses associated with a specific user's ID.
    // This is crucial for ensuring users can only see their own expenses.
    List<Expense> findByUserId(Long userId);

}