package com.lipika.expense_splitter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateExpenseRequest(
        @NotBlank String description,
        @Positive long amountInCents,
        @NotNull Long paidByUserId,
        @NotEmpty List<Long> splitAmongUserIds
) {}