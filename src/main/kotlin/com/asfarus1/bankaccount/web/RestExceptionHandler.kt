package com.asfarus1.bankaccount.web

import com.asfarus1.bankaccount.exceptions.NotEnoughBalance
import com.asfarus1.bankaccount.exceptions.NotFoundException
import com.asfarus1.bankaccount.exceptions.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handle(ex: NotFoundException): ResponseEntity<*> {
        return status(HttpStatus.NOT_FOUND).body(ex.localizedMessage)
    }

    @ExceptionHandler(NotEnoughBalance::class)
    fun handle(ex: NotEnoughBalance): ResponseEntity<*> {
        return status(HttpStatus.FORBIDDEN).body(ex.localizedMessage)
    }

    @ExceptionHandler(ValidationException::class)
    fun handle(ex: ValidationException): ResponseEntity<*> {
        return status(HttpStatus.BAD_REQUEST).body(ex.localizedMessage)
    }
}