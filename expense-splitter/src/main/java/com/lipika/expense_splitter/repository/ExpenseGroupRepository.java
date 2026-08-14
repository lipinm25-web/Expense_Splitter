package com.lipika.expense_splitter.repository;

import com.lipika.expense_splitter.model.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {
}