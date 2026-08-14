package com.lipika.expense_splitter.algorithm;

import java.util.*;

public class DebtSimplifier {

    public record Transaction(Long fromUserId, Long toUserId, long amountInCents) {}

    public List<Transaction> simplify(Map<Long, Long> netBalances) {
        PriorityQueue<long[]> creditors = new PriorityQueue<>((a, b) -> Long.compare(b[1], a[1]));
        PriorityQueue<long[]> debtors = new PriorityQueue<>((a, b) -> Long.compare(Math.abs(b[1]), Math.abs(a[1])));

        for (Map.Entry<Long, Long> entry : netBalances.entrySet()) {
            long userId = entry.getKey();
            long balance = entry.getValue();
            if (balance > 0) {
                creditors.add(new long[]{userId, balance});
            } else if (balance < 0) {
                debtors.add(new long[]{userId, balance});
            }
        }

        List<Transaction> transactions = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            long[] creditor = creditors.poll();
            long[] debtor = debtors.poll();

            long creditAmount = creditor[1];
            long debtAmount = Math.abs(debtor[1]);
            long settled = Math.min(creditAmount, debtAmount);

            transactions.add(new Transaction(debtor[0], creditor[0], settled));

            long remainingCredit = creditAmount - settled;
            long remainingDebt = debtAmount - settled;

            if (remainingCredit > 0) {
                creditors.add(new long[]{creditor[0], remainingCredit});
            }
            if (remainingDebt > 0) {
                debtors.add(new long[]{debtor[0], -remainingDebt});
            }
        }

        return transactions;
    }
}