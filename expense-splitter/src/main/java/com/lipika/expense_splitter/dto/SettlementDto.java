package com.lipika.expense_splitter.dto;

public record SettlementDto(Long fromUserId, String fromUserName, Long toUserId, String toUserName, long amountInCents) {}