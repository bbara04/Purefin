package hu.bbara.purefin.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaRepositoryModule {

    @Binds
    abstract fun bindMediaRepository(impl: InMemoryMediaRepository): MediaRepository
}
