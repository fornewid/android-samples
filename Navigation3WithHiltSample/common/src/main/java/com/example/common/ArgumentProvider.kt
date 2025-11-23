package com.example.common

import androidx.savedstate.SavedState
import kotlin.reflect.KClass

@DslMarker public annotation class ArgumentDsl

inline fun <T : Any> argumentProvider(
    builder: ArgumentProviderScope<T>.() -> Unit,
): (T) -> SavedState? = ArgumentProviderScope<T>().apply(builder).build()

@ArgumentDsl
class ArgumentProviderScope<T : Any> {
    private val clazzProviders = mutableMapOf<KClass<out T>, ArgumentClassProvider<out T>>()
    private val providers = mutableMapOf<Any, ArgumentProvider<out T>>()

    fun <K : T> addArgumentProvider(
        key: K,
        argument: (K) -> SavedState?,
    ) {
        require(key !in providers) {
            "An `argument` with the key `key` has already been added: ${key}."
        }
        providers[key] = ArgumentProvider(key, argument)
    }

    fun <K : T> ArgumentProviderScope<T>.argument(
        key: K,
        content: (K) -> SavedState?,
    ) {
        addArgumentProvider(key, content)
    }

    fun <K : T> addArgumentProvider(
        clazz: KClass<out K>,
        argument: (K) -> SavedState?,
    ) {
        require(clazz !in clazzProviders) {
            "An `argument` with the same `clazz` has already been added: ${clazz.simpleName}."
        }
        clazzProviders[clazz] = ArgumentClassProvider(clazz, argument)
    }

    inline fun <reified K : T> argument(
        noinline argument: (K) -> SavedState?,
    ) {
        addArgumentProvider(K::class, argument)
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun build(): (T) -> SavedState? = { key ->
        val argumentClassProvider = clazzProviders[key::class] as? ArgumentClassProvider<T>
        val argumentProvider = providers[key] as? ArgumentProvider<T>
        argumentClassProvider?.run { argument(key) }
            ?: argumentProvider?.run { argument(key) }
    }
}

private data class ArgumentClassProvider<K : Any>(
    val clazz: KClass<K>,
    val argument: (K) -> SavedState?,
)

private data class ArgumentProvider<K : Any>(
    val key: K,
    val argument: (K) -> SavedState?,
)
