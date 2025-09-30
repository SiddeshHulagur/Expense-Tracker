package com.siddesh.expensetracker.service;

import com.siddesh.expensetracker.entity.Expense;
import java.util.List;
import java.util.Optional;

public interface ExpenseService {

    List<Expense> getAllExpensesByUserId(Long userId);

    Optional<Expense> getExpenseByIdAndUserId(Long id, Long userId);

    Expense createExpense(Expense expense, Long userId);

    Optional<Expense> updateExpense(Long id, Expense expenseDetails, Long userId);

    void deleteExpense(Long id, Long userId);
}