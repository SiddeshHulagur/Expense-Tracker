package com.siddesh.expensetracker.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.siddesh.expensetracker.mongo.document.ExpenseDocument;

public interface ExpenseDocumentRepository extends MongoRepository<ExpenseDocument, String> {

    List<ExpenseDocument> findByUserId(Long userId);

    Optional<ExpenseDocument> findByExpenseIdAndUserId(Long expenseId, Long userId);

    Optional<ExpenseDocument> findByExpenseId(Long expenseId);
}
