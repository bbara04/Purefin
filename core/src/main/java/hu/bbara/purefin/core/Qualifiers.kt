package hu.bbara.purefin.core

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Online

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Offline