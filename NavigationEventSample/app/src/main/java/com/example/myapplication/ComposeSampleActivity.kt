package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.NavigationEventTransitionState.Companion.TRANSITIONING_BACK
import androidx.navigationevent.NavigationEventTransitionState.Companion.TRANSITIONING_FORWARD
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import kotlin.math.abs

class ComposeSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleTheme {
                NavigationEventScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationEventScreen() {
    val navigationOwner = rememberNavigationEventDispatcherOwner(enabled = true, parent = null)
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navigationOwner) {

        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        val eventDispatcher = navigationOwner.navigationEventDispatcher
        val eventInput = remember { GestureNavigationEventInput() }
        remember(eventDispatcher, eventInput) {
            eventDispatcher.addInput(
                input = eventInput,
                priority = NavigationEventDispatcher.PRIORITY_DEFAULT
            )
            true
        }

        val navigationState = rememberNavigationEventState(
            currentInfo = GestureNavigationInfo,
            backInfo = emptyList(),
            forwardInfo = emptyList()
        )
        NavigationEventHandler(
            state = navigationState,
            isBackEnabled = true,
            isForwardEnabled = true,
            onBackCompleted = {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Go back!")
                }
            },
            onBackCancelled = {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Cancel...")
                }
            },
            onForwardCompleted = {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Go forward!")
                }
            },
            onForwardCancelled = {
                coroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Cancel...")
                }
            }
        )

        var backArrowAlpha by remember { mutableStateOf(0f) }
        var backArrowTranslationX by remember { mutableStateOf(0f) }
        var backArrowTranslationY by remember { mutableStateOf(0f) }

        var forwardArrowAlpha by remember { mutableStateOf(0f) }
        var forwardArrowTranslationX by remember { mutableStateOf(0f) }
        var forwardArrowTranslationY by remember { mutableStateOf(0f) }

        when (val transitionState = navigationState.transitionState) {
            is NavigationEventTransitionState.Idle -> {
                backArrowAlpha = 0f
                backArrowTranslationX = 0f
                forwardArrowAlpha = 0f
            }

            is NavigationEventTransitionState.InProgress -> {
                val arrowDistance = LocalViewConfiguration.current.touchSlop * 2
                val event = transitionState.latestEvent
                val direction = transitionState.direction

                when (direction) {
                    TRANSITIONING_BACK -> {
                        backArrowAlpha = event.progress
                        backArrowTranslationX = arrowDistance * event.progress
                        backArrowTranslationY = event.touchY
                    }

                    TRANSITIONING_FORWARD -> {
                        forwardArrowAlpha = event.progress
                        forwardArrowTranslationX = -(arrowDistance * event.progress)
                        forwardArrowTranslationY = event.touchY
                    }
                }
            }
        }

        Scaffold(
            topBar = { TopAppBar(title = { Text(text = "Compose Sample") }) },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "Swipe left or right.",
                    modifier = Modifier.align(Alignment.Center),
                )

                GestureArea(
                    direction = GestureDirection.Back,
                    eventInput = eventInput,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemGestures.only(WindowInsetsSides.Left))
                        .align(Alignment.CenterStart),
                )

                GestureArea(
                    direction = GestureDirection.Forward,
                    eventInput = eventInput,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemGestures.only(WindowInsetsSides.Right))
                        .align(Alignment.CenterEnd),
                )

                Icon(
                    painterResource(R.drawable.ic_arrow_circle_left),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopStart)
                        .graphicsLayer {
                            alpha = backArrowAlpha
                            translationX = backArrowTranslationX
                            translationY = backArrowTranslationY
                        },
                    tint = MaterialTheme.colorScheme.primary,
                )

                Icon(
                    painterResource(R.drawable.ic_arrow_circle_right),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            alpha = forwardArrowAlpha
                            translationX = forwardArrowTranslationX
                            translationY = forwardArrowTranslationY
                        },
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun GestureArea(
    direction: GestureDirection,
    eventInput: GestureNavigationEventInput,
    modifier: Modifier = Modifier,
) {
    var startOffset: Offset by remember { mutableStateOf(Offset.Zero) }
    var recentOffset: Offset by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        startOffset = offset

                        val event = NavigationEvent(
                            touchX = offset.x,
                            touchY = offset.y,
                        )
                        when (direction) {
                            GestureDirection.Back -> eventInput.onBackStarted(event)
                            GestureDirection.Forward -> eventInput.onForwardStarted(event)
                        }
                    },
                    onDragEnd = {
                        val maxDistance = viewConfiguration.touchSlop * 4
                        val diffX = recentOffset.x - startOffset.x
                        val isComplete = when (direction) {
                            GestureDirection.Back -> diffX > maxDistance
                            GestureDirection.Forward -> diffX < -maxDistance
                        }
                        if (isComplete) {
                            when (direction) {
                                GestureDirection.Back -> eventInput.onBackCompleted()
                                GestureDirection.Forward -> eventInput.onForwardCompleted()
                            }
                        } else {
                            when (direction) {
                                GestureDirection.Back -> eventInput.onBackCancelled()
                                GestureDirection.Forward -> eventInput.onForwardCancelled()
                            }
                        }
                    },
                    onDragCancel = {
                        when (direction) {
                            GestureDirection.Back -> eventInput.onBackCancelled()
                            GestureDirection.Forward -> eventInput.onForwardCancelled()
                        }
                    },
                    onHorizontalDrag = { change, _ ->
                        recentOffset = change.position

                        val threshold = viewConfiguration.touchSlop
                        val x = change.position.x
                        val diffX = x - startOffset.x

                        val isOverThreshold = when (direction) {
                            GestureDirection.Back -> diffX > threshold
                            GestureDirection.Forward -> diffX < -threshold
                        }
                        if (isOverThreshold) {
                            val maxDistance = threshold * 4
                            val distance = abs(diffX) - threshold
                            val progress = distance / (maxDistance - threshold)
                            val event = NavigationEvent(
                                progress = progress.coerceIn(0f, 1f),
                                touchX = x,
                                touchY = startOffset.y,
                            )
                            when (direction) {
                                GestureDirection.Back -> eventInput.onBackProgressed(event)
                                GestureDirection.Forward -> eventInput.onForwardProgressed(event)
                            }
                        } else {
                            val event = NavigationEvent(
                                touchX = x,
                                touchY = startOffset.y,
                            )
                            when (direction) {
                                GestureDirection.Back -> eventInput.onBackProgressed(event)
                                GestureDirection.Forward -> eventInput.onForwardProgressed(event)
                            }
                        }
                    },
                )
            }
    )
}

@Preview
@Composable
private fun NavigationEventScreenPreview() {
    SampleTheme {
        NavigationEventScreen()
    }
}
