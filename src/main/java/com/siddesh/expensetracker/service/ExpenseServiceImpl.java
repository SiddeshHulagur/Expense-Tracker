package com.siddesh.expensetracker.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.siddesh.expensetracker.entity.Expense;
import com.siddesh.expensetracker.entity.User;
import com.siddesh.expensetracker.mongo.document.ExpenseDocument;
import com.siddesh.expensetracker.mongo.repository.ExpenseDocumentRepository;
import com.siddesh.expensetracker.mongo.service.SequenceGeneratorService;
import com.siddesh.expensetracker.repository.UserRepository;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private static final String EXPENSE_SEQUENCE = "expense_sequence";

    private final ExpenseDocumentRepository expenseDocumentRepository;
    private final UserRepository userRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public ExpenseServiceImpl(ExpenseDocumentRepository expenseDocumentRepository,
                              UserRepository userRepository,
                              SequenceGeneratorService sequenceGeneratorService) {
        this.expenseDocumentRepository = expenseDocumentRepository;
        this.userRepository = userRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public List<Expense> getAllExpensesByUserId(Long userId) {
        User user = getUserOrThrow(userId);
        return expenseDocumentRepository.findByUserId(userId)
                .stream()
                .map(document -> toExpense(document, user))
                .toList();
    }

    @Override
    public Optional<Expense> getExpenseByIdAndUserId(Long id, Long userId) {
        User user = getUserOrThrow(userId);
        return expenseDocumentRepository.findByExpenseIdAndUserId(id, userId)
                .map(document -> toExpense(document, user));
    }

    @Override
    public Expense createExpense(Expense expense, Long userId) {
        User user = getUserOrThrow(userId);
        long nextId = sequenceGeneratorService.getNextSequence(EXPENSE_SEQUENCE);

        ExpenseDocument document = ExpenseDocument.builder()
                .expenseId(nextId)
                .userId(userId)
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .category(expense.getCategory())
                .build();

        ExpenseDocument saved = expenseDocumentRepository.save(document);
        return toExpense(saved, user);
    }

    @Override
    public Optional<Expense> updateExpense(Long id, Expense expenseDetails, Long userId) {
        User user = getUserOrThrow(userId);
        return expenseDocumentRepository.findByExpenseIdAndUserId(id, userId)
                .map(document -> {
                    document.setDescription(expenseDetails.getDescription());
                    document.setAmount(expenseDetails.getAmount());
                    document.setDate(expenseDetails.getDate());
                    document.setCategory(expenseDetails.getCategory());
                    ExpenseDocument saved = expenseDocumentRepository.save(document);
                    return toExpense(saved, user);
                });
    }

    @Override
    public void deleteExpense(Long id, Long userId) {
        expenseDocumentRepository.findByExpenseIdAndUserId(id, userId)
                .ifPresent(expenseDocumentRepository::delete);
    }

    private Expense toExpense(ExpenseDocument document, User user) {
        return new Expense(
                document.getExpenseId(),
                document.getDescription(),
                document.getAmount(),
                document.getDate(),
                document.getCategory(),
                user
        );
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }
}