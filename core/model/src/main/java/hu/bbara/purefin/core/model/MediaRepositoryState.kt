package hu.bbara.purefin.core.model

sealed interface MediaRepositoryState {
    data object Loading : MediaRepositoryState
    data object Ready : MediaRepositoryState
    data class Error(val throwable: Throwable) : MediaRepositoryState
}
