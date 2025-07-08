package com.ceo.ticketissuance.core.util

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    data class Loading(val isLoading: Boolean = true) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
            is Loading -> Loading(isLoading)
        }
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) {
            action(exception)
        }
        return this
    }

    inline fun onLoading(action: (Boolean) -> Unit): Result<T> {
        if (this is Loading) {
            action(isLoading)
        }
        return this
    }
}

inline fun <T> Result<T>.fold(
    onSuccess: (T) -> Unit,
    onError: (Throwable) -> Unit,
    onLoading: (Boolean) -> Unit = {}
) {
    when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Error -> onError(exception)
        is Result.Loading -> onLoading(isLoading)
    }
}