package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultPredictivePopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.example.common.BottomSheetSceneStrategy
import com.example.common.EntryProviderInstaller
import com.example.common.Navigator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderScopes: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleTheme {
                StandardMainScreen(navigator, entryProviderScopes)
//                CustomMainScreen(navigator, entryProviderScopes)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun StandardMainScreen(
    navigator: Navigator,
    entryProviderScopes: Set<EntryProviderInstaller>,
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<Any>(directive = directive)
    val dialogStrategy = remember { DialogSceneStrategy<Any>() }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<Any>() }
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        sceneStrategy = listDetailStrategy then dialogStrategy then bottomSheetStrategy,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entryProviderScopes.forEach { builder -> this.builder() }
        }
    )

    LaunchedEffect(Unit) {
        snapshotFlow { navigator.backStack.toList() }
            .collectLatest { currentBackStack ->
                Log.d("MainActivity", "backStack=$currentBackStack")
            }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun CustomMainScreen(
    navigator: Navigator,
    entryProviderScopes: Set<EntryProviderInstaller>,
) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<Any>(directive = directive)
    val dialogStrategy = remember { DialogSceneStrategy<Any>() }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<Any>() }

    val entries =
        rememberDecoratedNavEntries(
            backStack = navigator.backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entryProviderScopes.forEach { builder -> this.builder() }
            },
        )

    val sceneStrategy = listDetailStrategy then dialogStrategy then bottomSheetStrategy
    val onBack = { navigator.goBack() }

    val sceneState = rememberSceneState(entries, sceneStrategy, onBack)
    val scene = sceneState.currentScene

    // Predictive Back Handling
    val currentInfo = SceneInfo(scene)
    val previousSceneInfos = sceneState.previousScenes.map { SceneInfo(it) }
    val gestureState =
        rememberNavigationEventState(currentInfo = currentInfo, backInfo = previousSceneInfos)

    NavigationBackHandler(
        state = gestureState,
        isBackEnabled = scene.previousEntries.isNotEmpty(),
        onBackCompleted = {
            // If `enabled` becomes stale (e.g., it was set to false but a gesture was
            // dispatched in the same frame), this may result in no entries being popped
            // due to entries.size being smaller than scene.previousEntries.size
            // but that's preferable to crashing with an IndexOutOfBoundsException
//            repeat(entries.size - scene.previousEntries.size) { onBack() }
            onBack()
        },
    )

    NavDisplay(
        sceneState = sceneState,
        navigationEventState = gestureState,
        modifier = Modifier,
        contentAlignment = Alignment.TopStart,
        sizeTransform = null,
        transitionSpec = defaultTransitionSpec(),
        popTransitionSpec = defaultPopTransitionSpec(),
        predictivePopTransitionSpec = defaultPredictivePopTransitionSpec(),
    )

    LaunchedEffect(Unit) {
        snapshotFlow { navigator.backStack.toList() }
            .collectLatest { currentBackStack ->
                Log.d("MainActivity", "backStack=$currentBackStack")
            }
    }
}
