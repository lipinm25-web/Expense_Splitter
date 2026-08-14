package com.lipika.expense_splitter.service;

import com.lipika.expense_splitter.dto.CreateExpenseRequest;
import com.lipika.expense_splitter.model.Expense;
import com.lipika.expense_splitter.model.ExpenseGroup;
import com.lipika.expense_splitter.model.ExpenseSplit;
import com.lipika.expense_splitter.model.User;
import com.lipika.expense_splitter.repository.ExpenseGroupRepository;
import com.lipika.expense_splitter.repository.ExpenseRepository;
import com.lipika.expense_splitter.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseGroupRepository groupRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          ExpenseGroupRepository groupRepository,
                          UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public void createExpense(Long groupId, CreateExpenseRequest request) {
        ExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        User paidBy = userRepository.findById(request.paidByUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.paidByUserId()));

        Expense expense = new Expense(request.description(), request.amountInCents(), paidBy, group);

        int numSharers = request.splitAmongUserIds().size();
        long baseShare = request.amountInCents() / numSharers;
        long remainder = request.amountInCents() % numSharers;

        for (int i = 0; i < request.splitAmongUserIds().size(); i++) {
            Long userId = request.splitAmongUserIds().get(i);
            User owedBy = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            long share = baseShare + (i < remainder ? 1 : 0);
            expense.addSplit(new ExpenseSplit(owedBy, share));
        }

        expenseRepository.save(expense);
    }
}