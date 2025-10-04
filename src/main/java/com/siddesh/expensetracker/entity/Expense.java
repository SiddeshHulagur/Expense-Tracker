package com.siddesh.expensetracker.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    private Long id;

    private String description;

    private BigDecimal amount;

    private LocalDate date;

    private String category;

    @JsonIgnore
    private User user;
}