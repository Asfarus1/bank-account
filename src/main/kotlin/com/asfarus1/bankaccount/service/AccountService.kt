package com.asfarus1.bankaccount.service

import com.asfarus1.bankaccount.exceptions.NotEnoughBalance
import com.asfarus1.bankaccount.exceptions.NotFoundException
import com.asfarus1.bankaccount.exceptions.ValidationException
import com.asfarus1.bankaccount.model.Account
import com.asfarus1.bankaccount.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class AccountService(private val repository: AccountRepository) {

    @Throws(NotFoundException::class)
    fun findById(id: Long): Account {
        return repository.findById(id)
                .orElseThrow { notFound(id) }
    }

    @Transactional
    @Throws(NotFoundException::class, NotEnoughBalance::class)
    fun withdrawal(id: Long, sum: BigDecimal) {
        checkPositive(sum)
        getForUpdate(id).also {
            if (it.balance < sum) throw NotEnoughBalance("Not enough money")
            it.balance -= sum
        }
    }

    @Transactional
    @Throws(NotFoundException::class)
    fun deposit(id: Long, sum: BigDecimal) {
        checkPositive(sum)
        getForUpdate(id).also { it.balance += sum }
    }

    fun createAccount(): Account {
        return repository.save(Account())
    }

    private fun notFound(id: Long) = NotFoundException("Account with id='${id}' not found")

    private fun checkPositive(sum: BigDecimal) {
        if (sum <= BigDecimal.ZERO) throw ValidationException("Sum must be positive")
    }

    private fun getForUpdate(id: Long) = repository.findForUpdate(id).orElseThrow { notFound(id) }
}
