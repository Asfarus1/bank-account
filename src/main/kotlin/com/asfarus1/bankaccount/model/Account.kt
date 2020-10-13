package com.asfarus1.bankaccount.model

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "accounts")
data class Account(
        @Id
        @GeneratedValue
        @Column(name = "id")
        var id: Long? = null,
        @Column(name = "balance", nullable = false)
        var balance: BigDecimal = BigDecimal.ZERO)