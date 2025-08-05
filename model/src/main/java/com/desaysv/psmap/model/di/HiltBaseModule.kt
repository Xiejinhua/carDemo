package com.desaysv.psmap.model.di

import com.desaysv.psmap.base.auto.layerstyle.IPrepareStyleFactory
import com.desaysv.psmap.model.layerstyle.PrepareStyleFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author uidq0728
 * @time 2023-7-17
 * @description di注入管理
 */
@Module
@InstallIn(SingletonComponent::class)
interface HiltBaseModule {
    @Binds
    @Singleton
    fun bindPrepareStyleFactory(prepareStyleFactory: PrepareStyleFactory): IPrepareStyleFactory

}
