package com.kurz.dbtransactions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Moves money between two accounts as a single, atomic business operation.
 * The whole method runs inside one transaction: if crediting the
 * destination account fails after the source account has already been
 * debited, the transaction rolls back and undoes the debit too - the
 * commit only happens once every step succeeds, per the article's
 * "separate what to update from whether to commit it" principle.
 */
@Service
public class TransferService {

    private final AccountRepository accountRepository;

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, long amountCents) {
        Account from = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("unknown source account"));
        Account to = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("unknown destination account"));

        from.debit(amountCents);

        if (to.isClosed()) {
            throw new IllegalStateException("destination account is closed");
        }

        to.credit(amountCents);
    }
}
