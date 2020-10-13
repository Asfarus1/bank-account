package com.asfarus1.bankaccount.web

import com.asfarus1.bankaccount.model.Account
import com.asfarus1.bankaccount.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.net.URI

@RestController
@RequestMapping("/accounts")
class AccountController(private val service: AccountService) {

    @PostMapping
    fun create(): ResponseEntity<Account> {
        val account = service.createAccount()
        return ResponseEntity.created(URI.create("/accounts/${account.id}")).body(account)
    }

    @GetMapping("{id}")
    fun get(@PathVariable id: Long): ResponseEntity<Account> {
        val account = service.findById(id)
        return ResponseEntity.ok().body(account)
    }

    @PostMapping("{id}/withdrawal")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun withdrawal(@PathVariable id: Long, @RequestBody sum: BigDecimal) {
        service.withdrawal(id, sum)
    }

    @PostMapping("{id}/deposit")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun deposit(@PathVariable id: Long, @RequestBody sum: BigDecimal) {
        service.deposit(id, sum)
    }
}