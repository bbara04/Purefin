package hu.bbara.purefin.data

sealed interface MediaRepositoryState {
    data object Loading : MediaRepositoryState
    data object Ready : MediaRepositoryState
    data class Error(val throwable: Throwable) : MediaRepositoryState
}
