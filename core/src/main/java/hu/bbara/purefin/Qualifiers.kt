package hu.bbara.purefin

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Online

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Offline