package com.asfarus1.bankaccount.web

import com.asfarus1.bankaccount.model.Account
import com.asfarus1.bankaccount.repository.AccountRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.stream.IntStream

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class AccountControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var repository: AccountRepository

    private val notExistedId = 999999L

    @Test
    fun create() {
        val body = mockMvc.perform(post("/accounts"))
                .andDo(print())
                .andExpect(status().isCreated)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn().response.contentAsString
        val account = mapper.readValue(body, Account::class.java)
        assertThat(account.balance, comparesEqualTo(BigDecimal.ZERO))
    }

    @Test
    fun get() {
        val createdAccount = createAccount()
        val body = findMockMvc(createdAccount.id, OK)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn().response.contentAsString
        val account = mapper.readValue(body, Account::class.java)
        assertThat(account.id, equalTo(createdAccount.id))
        assertThat(account.balance, comparesEqualTo(createdAccount.balance))
    }


    @Test
    fun `get - non-existent id`() {
        findMockMvc(notExistedId, NOT_FOUND)
                .andExpect(content().string(equalTo("Account with id='${notExistedId}' not found")))
    }

    @Test
    fun withdrawal() {
        val createdAccount = createAccount()
        val sum = BigDecimal.ONE
        withdrawalMockMvc(createdAccount.id, sum, ACCEPTED)
        accountHasBalance(createdAccount.id!!, createdAccount.balance - sum)
    }

    @Test
    fun `withdrawal - balance isn't enough`() {
        val createdAccount = createAccount()
        withdrawalMockMvc(createdAccount.id, createdAccount.balance + BigDecimal.ONE, FORBIDDEN)
                .andExpect(content().string(equalTo("Not enough money")))
    }

    @Test
    fun `withdrawal - non-existent id`() {
        withdrawalMockMvc(notExistedId, BigDecimal.ONE, NOT_FOUND)
                .andExpect(content().string(equalTo("Account with id='${notExistedId}' not found")))
    }

    @Test
    fun `withdrawal - negative sum`() {
        val createdAccount = createAccount()
        withdrawalMockMvc(createdAccount.id, -BigDecimal.ONE, BAD_REQUEST)
                .andExpect(content().string(equalTo("Sum must be positive")))
    }

    @Test
    fun `withdrawal - lost update test`() {
        val balance = BigDecimal(10000)
        val createdAccount = repository.save(Account(balance = balance))

        val content = post("/accounts/${createdAccount.id}/withdrawal")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(BigDecimal.ONE))

        IntStream.range(0, balance.intValueExact())
                .parallel()
                .forEach { mockMvc.perform(content) }

        accountHasBalance(createdAccount.id!!, BigDecimal.ZERO)
    }

    @Test
    fun deposit() {
        val createdAccount = createAccount()
        val sum = BigDecimal.ONE
        depositMockMvc(createdAccount.id, sum, ACCEPTED)
        accountHasBalance(createdAccount.id!!, createdAccount.balance + sum)
    }

    @Test
    fun `deposit - non-existent id`() {
        depositMockMvc(notExistedId, BigDecimal.ONE, NOT_FOUND)
                .andExpect(content().string(equalTo("Account with id='${notExistedId}' not found")))
    }

    @Test
    fun `deposit - negative sum`() {
        val createdAccount = createAccount()
        depositMockMvc(createdAccount.id, -BigDecimal.ONE, BAD_REQUEST)
                .andExpect(content().string(equalTo("Sum must be positive")))
    }

    @Test
    fun `deposit - lost update test`() {
        val createdAccount = createAccount()
        val sum = BigDecimal.ONE

        val content = post("/accounts/${createdAccount.id}/deposit")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(sum))

        val count = IntStream.range(1, 10000)
                .parallel()
                .peek { mockMvc.perform(content) }.count()

        accountHasBalance(createdAccount.id!!, createdAccount.balance + BigDecimal(count))
    }

    private fun createAccount() = repository.save(Account(balance = BigDecimal.TEN))

    private fun findMockMvc(id: Long?, status: HttpStatus): ResultActions {
        return mockMvc.perform(get("/accounts/${id}"))
                .andDo(print())
                .andExpect(status().`is`(status.value()))
    }

    private fun withdrawalMockMvc(id: Long?, sum: BigDecimal, status: HttpStatus): ResultActions {
        return postMockMvc("/accounts/${id}/withdrawal", sum, status)
    }

    private fun depositMockMvc(id: Long?, sum: BigDecimal, status: HttpStatus): ResultActions {
        return postMockMvc("/accounts/${id}/deposit", sum, status)
    }

    private fun accountHasBalance(id: Long, expectedBalance: BigDecimal) {
        val actual = repository.findById(id).orElse(null)
        assertThat(actual, notNullValue())
        assertThat(actual.balance, comparesEqualTo(expectedBalance))
    }

    private fun postMockMvc(url: String, content: Any, status: HttpStatus): ResultActions {
        return mockMvc.perform(post(url)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(content)))
                .andDo(print())
                .andExpect(status().`is`(status.value()))
    }
}