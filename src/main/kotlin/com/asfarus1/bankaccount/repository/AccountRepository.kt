package com.asfarus1.bankaccount.repository

import com.asfarus1.bankaccount.model.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.*
import javax.persistence.LockModeType

interface AccountRepository : JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a from Account a where a.id = :id")
    fun findForUpdate(id: Long): Optional<Account>
}