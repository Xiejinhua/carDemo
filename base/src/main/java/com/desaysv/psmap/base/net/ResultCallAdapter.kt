package com.desaysv.psmap.base.net

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import com.desaysv.psmap.base.utils.Result


class ResultCallAdapter<T>(private val responseType: Type) : CallAdapter<T, Result<T>> {
    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<T>): Result<T> {
        val response = call.execute()
        return if (response.isSuccessful) {
            val body = response.body()
            Result.success(body)
        } else {
            val errorBody = response.errorBody()
            val errorMessage = errorBody?.string() ?: "Unknown error"
            Result.error(Throwable(errorMessage))
        }
    }

    val factory: CallAdapter.Factory
        get() = object : CallAdapter.Factory() {
            override fun get(
                returnType: Type,
                annotations: Array<Annotation>,
                retrofit: Retrofit
            ): CallAdapter<*, *>? {
                if (getRawType(returnType) != Result::class.java) {
                    return null
                }
                val resultType = getParameterUpperBound(0, returnType as ParameterizedType)
                return ResultCallAdapter<Any>(resultType)
            }
        }
}