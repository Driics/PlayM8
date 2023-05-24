package ru.driics.playm8.domain.model

sealed class Response<out T> {
    object Loading : Response<Nothing>()

    object Empty : Response<Nothing>()

    data class Success<out T>(
        val data: T
    ) : Response<T>()

    data class Failure(
        val e: Exception
    ) : Response<Nothing>()
}