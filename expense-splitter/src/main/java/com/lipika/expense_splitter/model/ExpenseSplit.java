package com.lipika.expense_splitter.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owed_by_user_id")
    private User owedBy;

    @Column(nullable = false)
    private long amountOwedInCents;

    public ExpenseSplit(User owedBy, long amountOwedInCents) {
        this.owedBy = owedBy;
        this.amountOwedInCents = amountOwedInCents;
    }
}