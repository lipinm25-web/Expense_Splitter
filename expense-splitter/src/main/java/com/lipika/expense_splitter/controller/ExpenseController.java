package com.lipika.expense_splitter.controller;

import com.lipika.expense_splitter.dto.CreateExpenseRequest;
import com.lipika.expense_splitter.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createExpense(@PathVariable Long groupId, @Valid @RequestBody CreateExpenseRequest request) {
        expenseService.createExpense(groupId, request);
    }
}