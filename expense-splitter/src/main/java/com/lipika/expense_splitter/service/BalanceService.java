package com.lipika.expense_splitter.service;

import com.lipika.expense_splitter.model.Expense;
import com.lipika.expense_splitter.model.ExpenseSplit;
import com.lipika.expense_splitter.repository.ExpenseRepository;
import com.lipika.expense_splitter.repository.ExpenseSplitRepository;
import org.springframework.stereotype.Service;
import com.lipika.expense_splitter.algorithm.DebtSimplifier;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;

    public BalanceService(ExpenseRepository expenseRepository,
                          ExpenseSplitRepository expenseSplitRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
    }

    public Map<Long, Long> calculateNetBalances(Long groupId) {
        Map<Long, Long> balances = new HashMap<>();

        // Step 1: credit whoever paid for each expense
        for (Expense expense : expenseRepository.findByGroupId(groupId)) {
            Long payerId = expense.getPaidBy().getId();
            balances.merge(payerId, expense.getAmountInCents(), Long::sum);
        }

        // Step 2: debit whoever owes a share of each expense
        for (ExpenseSplit split : expenseSplitRepository.findByExpenseGroupId(groupId)) {
            Long owerId = split.getOwedBy().getId();
            balances.merge(owerId, -split.getAmountOwedInCents(), Long::sum);
        }

        return balances;
    }
    public List<DebtSimplifier.Transaction> getSettlementPlan(Long groupId) {
        Map<Long, Long> balances = calculateNetBalances(groupId);
        return new DebtSimplifier().simplify(balances);
    }

}