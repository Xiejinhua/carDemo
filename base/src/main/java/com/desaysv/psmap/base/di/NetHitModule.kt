package com.desaysv.psmap.base.di

import com.desaysv.psmap.base.net.ApiService
import com.desaysv.psmap.base.net.EResult
import com.desaysv.psmap.base.net.NetworkConstants
import com.desaysv.psmap.base.net.ResultCallAdapter
import com.desaysv.psmap.base.net.URLConfig
import com.desaysv.psmap.base.utils.AppExecutors
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetHitModule {
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, appExecutors: AppExecutors, factory: CallAdapter.Factory): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.from(appExecutors.networkIO())))
            .addCallAdapterFactory(factory)
            .callbackExecutor(appExecutors.networkIO())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(URLConfig.CUSTOM_CATEGORY_TEST_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(NetworkConstants.TIME_OUT_REQUEST.toLong(), TimeUnit.SECONDS)
            .connectTimeout(NetworkConstants.TIME_OUT_CONNECT.toLong(), TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.TIME_OUT_READ.toLong(), TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.TIME_OUT_WRITE.toLong(), TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAdapterFactory(resultCallAdapter: ResultCallAdapter<*>): CallAdapter.Factory {
        return resultCallAdapter.factory
    }

    @Provides
    @Singleton
    fun provideResultCallAdapter(): ResultCallAdapter<*> {
        val responseType: Type = object : TypeToken<EResult<*>>() {}.type
        return ResultCallAdapter<Any>(responseType)
    }
}