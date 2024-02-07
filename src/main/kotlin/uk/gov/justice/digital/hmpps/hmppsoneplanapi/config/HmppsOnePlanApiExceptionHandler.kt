package uk.gov.justice.digital.hmpps.hmppsoneplanapi.config

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import java.util.Locale

@RestControllerAdvice
class HmppsOnePlanApiExceptionHandler(
  private val messageSource: MessageSource,
) {

  @ExceptionHandler(ServerWebInputException::class)
  fun handleMissingParameter(e: ServerWebInputException): ResponseEntity<ErrorResponse> {
    val cause = e.cause?.cause
    if (cause is MismatchedInputException && cause.message?.contains("non-nullable") == true) {
      val message = "${cause.path.joinToString(".") { it.fieldName }}: is required"
      return ResponseEntity
        .status(BAD_REQUEST)
        .body(
          ErrorResponse(
            status = BAD_REQUEST,
            userMessage = message,
            developerMessage = message,
          ),
        ).also { log.info("Missing value: {}", e.message) }
    }
    return handleResponseStatusException(e)
  }

  @ExceptionHandler(WebExchangeBindException::class)
  fun handleValidationException(e: WebExchangeBindException): ResponseEntity<ErrorResponse> {
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
