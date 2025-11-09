package com.example.myapplication

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import com.example.myapplication.databinding.ViewSampleBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.math.abs

class ViewSampleActivity : AppCompatActivity() {

    private val eventDispatcher = NavigationEventDispatcher()
    private val eventInput = GestureNavigationEventInput()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val binding = ViewSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val windowInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(
                left = windowInsets.left,
                top = windowInsets.top,
                right = windowInsets.right,
                bottom = windowInsets.bottom,
            )
            val systemGesturesInsets = insets.getInsets(WindowInsetsCompat.Type.systemGestures())
            binding.backGestureArea.updateLayoutParams<MarginLayoutParams> {
                leftMargin = systemGesturesInsets.left
            }
            binding.forwardGestureArea.updateLayoutParams<MarginLayoutParams> {
                rightMargin = systemGesturesInsets.right
            }
            WindowInsetsCompat.CONSUMED
        }

        eventDispatcher.addInput(
            input = eventInput,
            priority = NavigationEventDispatcher.PRIORITY_DEFAULT,
        )

        val threshold: Int = ViewConfiguration.get(this).scaledTouchSlop
        val maxDistance: Int = threshold * 4
        binding.backGestureArea.setOnTouchListener(
            GestureTouchListener(
                direction = GestureDirection.Back,
                threshold = threshold,
                maxDistance = maxDistance,
                eventInput = eventInput,
            )
        )
        binding.forwardGestureArea.setOnTouchListener(
            GestureTouchListener(
                direction = GestureDirection.Forward,
                threshold = threshold,
                maxDistance = maxDistance,
                eventInput = eventInput,
            )
        )

        val arrowDistance: Int = threshold * 2
        val backHandler = object : NavigationEventHandler<NavigationEventInfo>(
            initialInfo = GestureNavigationInfo,
            isBackEnabled = true,
            isForwardEnabled = false,
        ) {
            override fun onBackStarted(event: NavigationEvent) {
                binding.backArrow.translationY = event.touchY
                binding.backArrow.alpha = 0f
                binding.backArrow.translationX = 0f
            }

            override fun onBackProgressed(event: NavigationEvent) {
                binding.backArrow.alpha = event.progress
                binding.backArrow.translationX = arrowDistance * event.progress
            }

            override fun onBackCompleted() {
                hideArrow()
                binding.root.showSnackbar("Go back!")
            }

            override fun onBackCancelled() {
                hideArrow()
                binding.root.showSnackbar("Cancel...")
            }

            private fun hideArrow() {
                binding.backArrow.animate()
                    .setDuration(150)
                    .alpha(0f)
            }
        }

        val forwardHandler = object : NavigationEventHandler<NavigationEventInfo>(
            initialInfo = GestureNavigationInfo,
            isBackEnabled = false,
            isForwardEnabled = true
        ) {
            override fun onForwardStarted(event: NavigationEvent) {
                binding.forwardArrow.translationY = event.touchY
                binding.forwardArrow.alpha = 0f
                binding.forwardArrow.translationX = 0f
            }

            override fun onForwardProgressed(event: NavigationEvent) {
                binding.forwardArrow.alpha = event.progress
                binding.forwardArrow.translationX = -(arrowDistance * event.progress)
            }

            override fun onForwardCompleted() {
                hideForwardArrow()
                binding.root.showSnackbar("Go forward!")
            }

            override fun onForwardCancelled() {
                hideForwardArrow()
                binding.root.showSnackbar("Cancel...")
            }

            private fun hideForwardArrow() {
                binding.forwardArrow.animate()
                    .setDuration(150)
                    .alpha(0f)
            }
        }

        eventDispatcher.addHandler(
            handler = backHandler,
            priority = NavigationEventDispatcher.PRIORITY_DEFAULT,
        )
        eventDispatcher.addHandler(
            handler = forwardHandler,
            priority = NavigationEventDispatcher.PRIORITY_DEFAULT,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        eventDispatcher.removeInput(eventInput)
    }

    private fun View.showSnackbar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
    }
}

private class GestureTouchListener(
    private val direction: GestureDirection,
    private val threshold: Int,
    private val maxDistance: Int,
    private val eventInput: GestureNavigationEventInput,
) : View.OnTouchListener {

    private var startX = 0f
    private var startY = 0f

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                startGesture(
                    event = NavigationEvent(
                        touchX = event.x,
                        touchY = event.y,
                    )
                )
            }

            MotionEvent.ACTION_MOVE -> {
                val diffX = event.x - startX
                val diffY = event.y - startY

                if (abs(diffX) > abs(diffY)) {
                    if (isOverThreshold(diffX)) {
                        progressGesture(
                            event = NavigationEvent(
                                progress = progressOf(diffX),
                                touchX = event.x,
                                touchY = event.y,
                            )
                        )
                    } else {
                        progressGesture(
                            event = NavigationEvent(
                                touchX = event.x,
                                touchY = event.y,
                            )
                        )
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 스와이프 종료
                val diffX = event.x - startX
                val diffY = event.y - startY

                if (isComplete(diffX) && abs(diffX) > abs(diffY)) {
                    completeGesture()
                } else {
                    cancelGesture()
                }
            }
        }
        return true
    }

    private fun isOverThreshold(diffX: Float): Boolean {
        return when (direction) {
            GestureDirection.Back -> diffX > threshold
            GestureDirection.Forward -> diffX < -threshold
        }
    }

    private fun isComplete(diffX: Float): Boolean {
        return when (direction) {
            GestureDirection.Back -> diffX > maxDistance
            GestureDirection.Forward -> diffX < -maxDistance
        }
    }

    private fun startGesture(event: NavigationEvent) {
        when (direction) {
            GestureDirection.Back -> eventInput.onBackStarted(event)
            GestureDirection.Forward -> eventInput.onForwardStarted(event)
        }
    }

    private fun progressGesture(event: NavigationEvent) {
        when (direction) {
            GestureDirection.Back -> eventInput.onBackProgressed(event)
            GestureDirection.Forward -> eventInput.onForwardProgressed(event)
        }
    }

    private fun completeGesture() {
        when (direction) {
            GestureDirection.Back -> eventInput.onBackCompleted()
            GestureDirection.Forward -> eventInput.onForwardCompleted()
        }
    }

    private fun cancelGesture() {
        when (direction) {
            GestureDirection.Back -> eventInput.onBackCancelled()
            GestureDirection.Forward -> eventInput.onForwardCancelled()
        }
    }

    private fun progressOf(diffX: Float): Float {
        val distance = abs(diffX) - threshold
        val progress = distance / (maxDistance - threshold)
        return progress.coerceIn(0f, 1f)
    }
}
