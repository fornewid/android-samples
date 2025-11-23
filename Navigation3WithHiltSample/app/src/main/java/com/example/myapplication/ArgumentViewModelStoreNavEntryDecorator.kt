@file:JvmName("ViewModelStoreNavEntryDecoratorKt")
@file:JvmMultifileClass

package com.example.myapplication

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

@Composable
fun <T : Any> rememberArgumentViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    removeViewModelStoreOnPop: () -> Boolean =
        ViewModelStoreNavEntryDecoratorDefaults.removeViewModelStoreOnPop(),
    argumentProvider: (contentKey: T) -> SavedState? = { null },
): ArgumentViewModelStoreNavEntryDecorator<T> {
    val currentRemoveViewModelStoreOnPop = rememberUpdatedState(removeViewModelStoreOnPop)
    return remember(viewModelStoreOwner, currentRemoveViewModelStoreOnPop) {
        ArgumentViewModelStoreNavEntryDecorator(
            viewModelStoreOwner.viewModelStore,
            removeViewModelStoreOnPop,
            argumentProvider,
        )
    }
}

/**
 * Provides the content of a [NavEntry] with a [ViewModelStoreOwner] and provides that
 * [ViewModelStoreOwner] as a [LocalViewModelStoreOwner] so that it is available within the content.
 *
 * This requires the usage of [androidx.navigation3.runtime.SaveableStateHolderNavEntryDecorator] to
 * ensure that the [NavEntry] scoped [ViewModel]s can properly provide access to
 * [SavedStateHandle]s
 *
 * @param [viewModelStore] The [ViewModelStore] that provides to NavEntries
 * @param [removeViewModelStoreOnPop] A lambda that returns a Boolean for whether the store for a
 *   [NavEntry] should be cleared when the [NavEntry] is popped from the backStack. If true, the
 *   entry's ViewModelStore will be removed.
 * @see NavEntryDecorator.onPop for more details on when this callback is invoked
 */
  public class ArgumentViewModelStoreNavEntryDecorator<T : Any>(
      viewModelStore: ViewModelStore,
      removeViewModelStoreOnPop: () -> Boolean,
      argumentProvider: (contentKey: T) -> SavedState?,
  ) :
      NavEntryDecorator<T>(
          onPop = ({ key ->
              if (removeViewModelStoreOnPop()) {
                  viewModelStore.getEntryViewModel().clearViewModelStoreOwnerForKey(key)
              }
          }),
          decorate = { entry ->
              val viewModelStore =
                  viewModelStore.getEntryViewModel().viewModelStoreForKey(entry.contentKey)

              val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
              val childViewModelStoreOwner = remember {
                  object :
                      ViewModelStoreOwner,
                      SavedStateRegistryOwner by savedStateRegistryOwner,
                      HasDefaultViewModelProviderFactory {
                      override val viewModelStore: ViewModelStore
                          get() = viewModelStore

                      override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                          get() = SavedStateViewModelFactory()

                      override val defaultViewModelCreationExtras: CreationExtras
                          get() =
                              MutableCreationExtras().also {
                                  it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
                                  it[VIEW_MODEL_STORE_OWNER_KEY] = this
                                  // The only changes
                                  argumentProvider(entry.privateKey)?.let { arguments ->
                                      it[DEFAULT_ARGS_KEY] = arguments
                                  }
                              }

                      init {
                          require(this.lifecycle.currentState == Lifecycle.State.INITIALIZED) {
                              "The Lifecycle state is already beyond INITIALIZED. The " +
                                      "ViewModelStoreNavEntryDecorator requires adding the " +
                                      "SavedStateNavEntryDecorator to ensure support for " +
                                      "SavedStateHandles."
                          }
                          enableSavedStateHandles()
                      }
                  }
              }
              CompositionLocalProvider(LocalViewModelStoreOwner provides childViewModelStoreOwner) {
                  entry.Content()
              }
          },
      )

private class EntryViewModel : ViewModel() {
    private val owners = mutableMapOf<Any, ViewModelStore>()

    fun viewModelStoreForKey(key: Any): ViewModelStore = owners.getOrPut(key) { ViewModelStore() }

    fun clearViewModelStoreOwnerForKey(key: Any) {
        owners.remove(key)?.clear()
    }

    override fun onCleared() {
        owners.forEach { (_, store) -> store.clear() }
    }
}

/** Holds the default functions for the [ArgumentViewModelStoreNavEntryDecorator]. */
public object ViewModelStoreNavEntryDecoratorDefaults {
    /**
     * Controls whether the [ArgumentViewModelStoreNavEntryDecorator] should clear the ViewModelStore scoped
     * to a [NavEntry] when [NavEntryDecorator.onPop] is invoked for that [NavEntry]'s
     * [NavEntry.contentKey]
     *
     * The ViewModelStore is cleared if this returns true. The store is retained if false.
     */
    @Composable
    @Suppress("PairedRegistration")
    fun removeViewModelStoreOnPop(): () -> Boolean {
        val activity = LocalActivity.current
        return { activity?.isChangingConfigurations != true }
    }
}

private fun ViewModelStore.getEntryViewModel(): EntryViewModel {
    val provider =
        ViewModelProvider.create(
            store = this,
            factory = viewModelFactory { initializer { EntryViewModel() } },
        )
    return provider[EntryViewModel::class]
}

/**
 * Access private key field via reflection
 */
@Suppress("UNCHECKED_CAST")
private val <T : Any> NavEntry<T>.privateKey: T
    get() = try {
        val keyField = NavEntry::class.java.getDeclaredField("key")
        keyField.isAccessible = true
        keyField.get(this) as? T ?: throw IllegalStateException("Key is null")
    } catch (e: Exception) {
        throw e
    }
