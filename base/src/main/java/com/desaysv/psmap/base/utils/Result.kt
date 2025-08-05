/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.desaysv.psmap.base.utils

import androidx.paging.LoadState

/**
 * author: uidq0964
 * email: Ronghua.Deng@desaysv.com
 * create on: 2023/6/28 12:02
 * description:
 */

/**
 * A generic class that holds a value with its loading status.
 */
data class Result<out T>(val status: Status, val data: T?, val throwable: Throwable?) {
    constructor(loadState: LoadState, data: T?) : this(
        when (loadState) {
            is LoadState.Loading -> Status.LOADING
            is LoadState.NotLoading -> Status.SUCCESS
            else -> Status.ERROR
        },
        data,
        (loadState as? LoadState.Error)?.error
    )

    companion object {
        fun <T> success(data: T?, throwable: Throwable? = null): Result<T> {
            return Result(Status.SUCCESS, data, throwable)
        }

        fun <T> error(throwable: Throwable): Result<T> {
            return Result(Status.ERROR, null, throwable)
        }

        fun <T> error(msg: String): Result<T> {
            return Result(Status.ERROR, null, Throwable(msg))
        }

        fun <T> loading(data: T?): Result<T> {
            return Result(Status.LOADING, data, null)
        }

        fun <T> loading(): Result<T> {
            return Result(Status.LOADING, null, null)
        }
    }

    fun isLoading(): Boolean {
        return status == Status.LOADING
    }

    fun isError(): Boolean {
        return status == Status.ERROR
    }

    fun isSuccess(): Boolean {
        return status == Status.SUCCESS
    }

    fun isEmpty(): Boolean {
        return status == Status.SUCCESS && (data == null || (data is List<*> && data.toList().isEmpty()))
    }

    fun isNotEmpty(): Boolean {
        return status == Status.SUCCESS && (data != null && (data !is List<*> || data.toList().isNotEmpty()))
    }

    /**
     * 转换Result的泛型数据，Loading、Error由此方法直接转换，而data则由实现的Function函数进行转换
     */
    inline fun <X> transform(transform: (T) -> X): Result<X> {
        return when (status) {
            Status.LOADING -> loading()
            Status.ERROR -> error(throwable ?: Throwable(""))
            else -> success(if (data == null) null else transform(data), throwable)
        }
    }
}
