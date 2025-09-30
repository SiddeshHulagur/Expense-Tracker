package com.siddesh.expensetracker.controller;

import com.siddesh.expensetracker.entity.Expense;
import com.siddesh.expensetracker.entity.User;
import com.siddesh.expensetracker.repository.UserRepository;
import com.siddesh.expensetracker.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository; // Inject UserRepository

    public ExpenseController(ExpenseService expenseService, UserRepository userRepository) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
    }

    // Helper method to get the current user's ID
    private Long getCurrentUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public List<Expense> getAllExpenses(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return expenseService.getAllExpensesByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        Expense createdExpense = expenseService.createExpense(expense, userId);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }



    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return expenseService.getExpenseByIdAndUserId(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @RequestBody Expense expenseDetails, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        return expenseService.updateExpense(id, expenseDetails, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        expenseService.deleteExpense(id, userId);
        return ResponseEntity.noContent().build();
    }
}