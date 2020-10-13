package com.asfarus1.bankaccount.service

import com.asfarus1.bankaccount.exceptions.NotEnoughBalance
import com.asfarus1.bankaccount.exceptions.NotFoundException
import com.asfarus1.bankaccount.exceptions.ValidationException
import com.asfarus1.bankaccount.model.Account
import com.asfarus1.bankaccount.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.Optional.empty
import java.util.Optional.of

internal class AccountServiceTest {

    private val mockRepository: AccountRepository = mock(AccountRepository::class.java)
    private val service: AccountService = AccountService(mockRepository)
    private val account = Account(1L, BigDecimal.TEN)
    private val notExistedId = 999999L

    @BeforeEach
    fun setUp() {
        `when`(mockRepository.findById(account.id!!)).thenReturn(of(account))
        `when`(mockRepository.findForUpdate(account.id!!)).thenReturn(of(account))
        `when`(mockRepository.findById(notExistedId)).thenReturn(empty())
        `when`(mockRepository.findForUpdate(notExistedId)).thenReturn(empty())
        `when`(mockRepository.save(any(Account::class.java))).thenReturn(account)
    }

    @Test
    fun findById() {
        val (id, balance) = service.findById(account.id!!)
        val expectedId = account.id
        assertEquals(expectedId, id)
        val expectedBalance = account.balance
        assertEquals(expectedBalance, balance)
    }

    @Test
    fun `findById - non-existent id`() {
        assertThrows(NotFoundException::class.java) { service.findById(notExistedId) }
    }

    @Test
    fun withdrawal() {
        service.withdrawal(account.id!!, BigDecimal.ONE)
    }

    @Test
    fun `withdrawal - balance isn't enough`() {
        assertThrows(NotEnoughBalance::class.java)
        { service.withdrawal(account.id!!, account.balance + BigDecimal.ONE) }
    }

    @Test
    fun `withdrawal - non-existent id`() {
        assertThrows(NotFoundException::class.java)
        { service.withdrawal(notExistedId, BigDecimal.ONE) }
    }

    @Test
    fun `withdrawal - negative sum`() {
        assertThrows(ValidationException::class.java)
        { service.withdrawal(account.id!!, -BigDecimal.ONE) }
    }

    @Test
    fun deposit() {
        service.deposit(account.id!!, BigDecimal.ONE)
    }

    @Test
    fun `deposit - non-existent id`() {
        assertThrows(NotFoundException::class.java)
        { service.deposit(notExistedId, BigDecimal.ONE) }
    }

    @Test
    fun `deposit - negative sum`() {
        assertThrows(ValidationException::class.java)
        { service.deposit(account.id!!, -BigDecimal.ONE) }
    }

    @Test
    fun createAccount() {
        assertEquals(account, service.createAccount())
    }
}