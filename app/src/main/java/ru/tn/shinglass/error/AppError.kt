package ru.tn.shinglass.error

sealed class AppError(var code: String): RuntimeException(code)
class ApiError(val status: Int, code: String): AppError(code)
class ApiServiceError(code: String = "Service API error"): AppError(code)
object NetworkError : AppError("error_network")
object UnknownError: AppError("error_unknown")
