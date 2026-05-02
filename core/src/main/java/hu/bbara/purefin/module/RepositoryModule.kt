package hu.bbara.purefin.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bbara.purefin.data.CompositeLocalMediaRepository
import hu.bbara.purefin.data.MediaProgressWriter
import hu.bbara.purefin.data.LocalMediaRepository


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMediaRepository(impl: CompositeLocalMediaRepository): LocalMediaRepository

    @Binds
    abstract fun bindMediaProgressWrite(impl: CompositeLocalMediaRepository): MediaProgressWriter
}
