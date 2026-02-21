package hu.bbara.purefin.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaRepositoryModule {

    @Binds
    abstract fun bindInMemoryMediaRepository(impl: InMemoryMediaRepository): MediaRepository

}
