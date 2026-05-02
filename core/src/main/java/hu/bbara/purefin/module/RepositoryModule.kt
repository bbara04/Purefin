package hu.bbara.purefin.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.CompositeMediaRepository
import hu.bbara.purefin.data.MediaProgressWriter
import hu.bbara.purefin.data.MediaRepository


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMediaRepository(impl: CompositeMediaRepository): MediaRepository

    @Binds
    abstract fun bindMediaProgressWrite(impl: CompositeMediaRepository): MediaProgressWriter
}
