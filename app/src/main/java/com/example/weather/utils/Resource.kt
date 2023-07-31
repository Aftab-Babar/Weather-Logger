package com.example.weather.utils

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val errorCode: Int? = null
) {

    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String?, errorCode: Int?, data: T? = null) :
        Resource<T>(data, message, errorCode)

    class Loading<T>() : Resource<T>()

    class LoadingMore<T>(data: T? = null) : Resource<T>(data)

}