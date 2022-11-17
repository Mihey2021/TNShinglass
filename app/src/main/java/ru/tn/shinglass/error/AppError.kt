package ru.tn.shinglass.error

import android.database.SQLException
import java.io.IOException

//sealed class AppError(var code: String): RuntimeException(code)
//class ApiError(val status: Int, code: String): AppError(code)
//class ApiServiceError(code: String = "Service API error"): AppError(code)
//object NetworkError : AppError("error_network")
//object UnknownError: AppError("error_unknown")

sealed class AppError(var code: String) : RuntimeException(code) {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is ApiServiceError -> e
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}

class ApiError(val status: Int, code: String) : AppError(code)
class ApiServiceError(code: String) : AppError(code)
object NetworkError : AppError("error_network")
object DbError : AppError("error_db")
object UnknownError : AppError("error_unknown")