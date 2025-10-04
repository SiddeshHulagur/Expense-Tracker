package com.siddesh.expensetracker.repository;

import com.siddesh.expensetracker.mongo.repository.ExpenseDocumentRepository;

/**
 * Alias retained for backwards compatibility. All expense persistence is handled by
 * {@link com.siddesh.expensetracker.mongo.repository.ExpenseDocumentRepository}.
 */
public interface ExpenseRepository extends ExpenseDocumentRepository {
}