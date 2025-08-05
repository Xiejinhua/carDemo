package com.desaysv.psmap.base.di

import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.data.IRouteRepository
import com.desaysv.psmap.base.data.NaviRepository
import com.desaysv.psmap.base.data.RouteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BindHiltModule {

    @Binds
    @Singleton
    fun bindIRouteRepository(repository: RouteRepository): IRouteRepository

    @Binds
    @Singleton
    fun bindINaviRepository(repository: NaviRepository): INaviRepository
}
