package com.example.weather.api

import android.content.Context
import com.example.weather.BuildConfig
import com.example.weather.utils.Tools.isInternetAvailable
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http2.ConnectionShutdownException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WeatherInterceptor(
    private val context: Context
) :
    Interceptor {

    //private var retryCount = 0

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()


        request = request.newBuilder().url(request.url.newBuilder().addQueryParameter("appid", BuildConfig.API_TOKEN).build())
            .build()

        return try {

            if (!context.isInternetAvailable()) {
                throw ConnectionShutdownException()
            }

            chain.proceed(request)
        } catch (e: Exception) {

            val message = when (e) {
                is SocketTimeoutException -> {
                    "Timeout - Please check your internet connection"
                }
                is UnknownHostException -> {
                    "Unable to make a connection. Please check your internet"
                }
                is ConnectionShutdownException -> {
                    "Connection shutdown. Please check your internet"
                }
                is IOException -> {
                    "Server is unreachable, please try again later."
                }
                is IllegalStateException -> {
                    e.message ?: "Something went wrong. - IllegalStateException"
                }
                else -> {
                    e.message ?: "Something went wrong."
                }
            }

            return Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                .code(999).message(message)
                .body("{${e.message}}".toResponseBody()).build()
        }


    }
}