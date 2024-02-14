package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.context.MessageSource
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsoneplanapi.exceptions.UpdateNotAllowedException
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

@RestControllerAdvice
class HmppsOnePlanApiExceptionHandler(
  private val messageSource: MessageSource,
) {

  @ExceptionHandler(ServerWebInputException::class)
  fun handleWebInputException(e: ServerWebInputException): ResponseEntity<ErrorResponse> {
    val cause = e.cause
    val subCause = e.cause?.cause
    return when {
      subCause is MismatchedInputException -> handleMismatchedInputCause(e, subCause)
      cause is TypeMismatchException -> handleTypeMismatchException(e, cause)
      else -> handleResponseStatusException(e)
    }
  }

  private fun handleTypeMismatchException(
    e: ServerWebInputException,
    cause: TypeMismatchException,
  ): ResponseEntity<ErrorResponse> {
    val detail = when (cause.requiredType) {
      UUID::class.java -> "should be a valid UUID"
      else -> "invalid value given"
    }

    val message = "${cause.propertyName}: $detail"
    log.info("Type mismatch: {}, expected: {}", e.message, cause.requiredType.simpleName)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = message,
          developerMessage = cause.message,
        ),
      )
  }

  private fun handleMismatchedInputCause(
    e: ServerWebInputException,
    cause: MismatchedInputException,
  ): ResponseEntity<ErrorResponse> {
    val message = "${variablePath(cause)}: ${mismatchedInputMessage(cause)}"
    log.info("Mismatched input: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = message,
          developerMessage = cause.originalMessage,
        ),
      )
  }

  private fun mismatchedInputMessage(cause: MismatchedInputException): String {
    if (cause.message?.contains("missing", ignoreCase = true) == true) {
      return "is required"
    }

    val type = cause.targetType
    return when {
      type == LocalDate::class.java -> "should be a date in format yyyy-MM-dd"
      type == Boolean::class.java -> "should be a boolean true|false"
      type.isEnum -> "should be one of [${type.enumConstants.joinToString { it.toString() }}]"
      else -> "is invalid"
    }
  }

  private fun variablePath(cause: MismatchedInputException) =
    cause.path.joinToString(".") { it.fieldName }

  @ExceptionHandler(WebExchangeBindException::class)
  fun handleBindValidationException(e: WebExchangeBindException): ResponseEntity<ErrorResponse> {
    if (e.reason != "Validation failure") {
      return handleResponseStatusException(e)
    }
    val message: String = e.bindingResult.fieldErrors
      .joinToString { fe -> "${fe.field}: ${messageSource.getMessage(fe, Locale.getDefault())}" }

    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = message,
          developerMessage = e.message,
        ),
      ).also { log.info("Validation exception: {}", e.message) }
  }

  @ExceptionHandler(ConstraintViolationException::class)
  fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
    val message = e.constraintViolations.joinToString { violation ->
      "${violation.propertyPath.drop(1).joinToString(".") { it.name }}: ${violation.message}"
    }
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = message,
          developerMessage = e.message,
        ),
      ).also { log.info("Validation exception: {}", e.message, e) }
  }

  @ExceptionHandler(ResponseStatusException::class)
  fun handleResponseStatusException(e: ResponseStatusException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(e.statusCode)
    .body(
      ErrorResponse(
        status = e.statusCode.value(),
        userMessage = e.message,
        developerMessage = e.message,
      ),
    ).also { log.info("Response status exception, {}", e.message, e) }

  @ExceptionHandler(DuplicateKeyException::class)
  fun handleDuplicateKeyException(e: DuplicateKeyException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(HttpStatus.UNPROCESSABLE_ENTITY)
    .body(
      ErrorResponse(
        status = UNPROCESSABLE_ENTITY.value(),
        userMessage = "unexpected error, please retry",
        developerMessage = e.message,
      ),
    ).also { log.info("Duplicate key exception, {}", e.message) }

  @ExceptionHandler(UpdateNotAllowedException::class)
  fun handleUpdateNotAllowed(e: UpdateNotAllowedException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST.value(),
        userMessage = "cannot update completed ${e.type.simpleName?.removeSuffix("Entity")}",
        developerMessage = e.message,
      ),
    ).also { log.info("Update not allowed: {}", e.message) }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        userMessage = "Unexpected error: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Unexpected exception", e) }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
