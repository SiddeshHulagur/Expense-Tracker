package com.siddesh.expensetracker.mongo.document;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "expenses")
public class ExpenseDocument {

    @Id
    private String id;

    @Field("expense_id")
    private Long expenseId;

    @Field("user_id")
    private Long userId;

    private String description;

    private BigDecimal amount;

    private LocalDate date;

    private String category;
}
