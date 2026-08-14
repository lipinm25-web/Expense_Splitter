package com.lipika.expense_splitter.dto;

import java.util.List;

public record ExpenseGroupDto(Long id, String name, List<UserDto> members) {}