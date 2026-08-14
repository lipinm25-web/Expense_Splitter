package com.lipika.expense_splitter.controller;

import com.lipika.expense_splitter.algorithm.DebtSimplifier;
import com.lipika.expense_splitter.dto.BalanceDto;
import com.lipika.expense_splitter.dto.SettlementDto;
import com.lipika.expense_splitter.repository.UserRepository;
import com.lipika.expense_splitter.service.BalanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/balances")
public class BalanceController {

    private final BalanceService balanceService;
    private final UserRepository userRepository;

    public BalanceController(BalanceService balanceService, UserRepository userRepository) {
        this.balanceService = balanceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<BalanceDto> getBalances(@PathVariable Long groupId) {
        Map<Long, Long> balances = balanceService.calculateNetBalances(groupId);
        return balances.entrySet().stream()
                .map(entry -> {
                    String name = userRepository.findById(entry.getKey())
                            .map(u -> u.getName())
                            .orElse("Unknown");
                    return new BalanceDto(entry.getKey(), name, entry.getValue());
                })
                .toList();
    }

    @GetMapping("/settlement-plan")
    public List<SettlementDto> getSettlementPlan(@PathVariable Long groupId) {
        List<DebtSimplifier.Transaction> transactions = balanceService.getSettlementPlan(groupId);
        return transactions.stream()
                .map(t -> new SettlementDto(
                        t.fromUserId(),
                        userRepository.findById(t.fromUserId()).map(u -> u.getName()).orElse("Unknown"),
                        t.toUserId(),
                        userRepository.findById(t.toUserId()).map(u -> u.getName()).orElse("Unknown"),
                        t.amountInCents()
                ))
                .toList();
    }
}