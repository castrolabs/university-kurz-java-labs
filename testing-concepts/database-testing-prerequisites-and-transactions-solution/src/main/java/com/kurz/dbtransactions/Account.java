package com.kurz.dbtransactions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A bank account: an owner name, a balance in cents, and whether the
 * account still accepts incoming transfers.
 */
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String owner;

    @Column(name = "balance_cents", nullable = false)
    private long balanceCents;

    @Column(nullable = false)
    private boolean closed;

    protected Account() {
        // required by JPA
    }

    public Account(String owner, long balanceCents, boolean closed) {
        this.owner = owner;
        this.balanceCents = balanceCents;
        this.closed = closed;
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public long getBalanceCents() {
        return balanceCents;
    }

    public boolean isClosed() {
        return closed;
    }

    public void debit(long amountCents) {
        if (amountCents > balanceCents) {
            throw new IllegalStateException("insufficient funds for account " + owner);
        }
        balanceCents -= amountCents;
    }

    public void credit(long amountCents) {
        balanceCents += amountCents;
    }
}
