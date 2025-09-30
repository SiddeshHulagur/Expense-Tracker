package com.siddesh.expensetracker.service;

import com.siddesh.expensetracker.entity.Expense;
import com.siddesh.expensetracker.entity.User;
import com.siddesh.expensetracker.repository.ExpenseRepository;
import com.siddesh.expensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service // Marks this class as a Spring Service component
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    // Constructor-based dependency injection (preferred method)
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Expense> getAllExpensesByUserId(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    public Optional<Expense> getExpenseByIdAndUserId(Long id, Long userId) {
        return expenseRepository.findById(id)
                .filter(expense -> expense.getUser().getId().equals(userId));
    }

    @Override
    public Expense createExpense(Expense expense, Long userId) {
        // Find the user by ID, or throw an exception if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        // Associate the expense with the user
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    @Override
    public Optional<Expense> updateExpense(Long id, Expense expenseDetails, Long userId) {
        return expenseRepository.findById(id)
                .filter(expense -> expense.getUser().getId().equals(userId)) // Ensure the expense belongs to the user
                .map(expense -> {
                    expense.setDescription(expenseDetails.getDescription());
                    expense.setAmount(expenseDetails.getAmount());
                    expense.setDate(expenseDetails.getDate());
                    expense.setCategory(expenseDetails.getCategory());
                    return expenseRepository.save(expense);
                });
    }

    @Override
    public void deleteExpense(Long id, Long userId) {
        expenseRepository.findById(id)
                .filter(expense -> expense.getUser().getId().equals(userId)) // Ensure the expense belongs to the user
                .ifPresent(expenseRepository::delete); // If it exists and belongs to the user, delete it
    }
}