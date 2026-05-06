package hu.bbara.purefin.core.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.core.data.CompositeLocalMediaRepository
import hu.bbara.purefin.core.data.LocalMediaRepository
import hu.bbara.purefin.core.data.LocalMediaUpdater


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMediaRepository(impl: CompositeLocalMediaRepository): LocalMediaRepository

    @Binds
    abstract fun bindMediaProgressWrite(impl: CompositeLocalMediaRepository): LocalMediaUpdater
}
