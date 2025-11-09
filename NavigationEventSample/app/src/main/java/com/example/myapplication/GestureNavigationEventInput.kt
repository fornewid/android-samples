package com.example.myapplication

import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventInput

class GestureNavigationEventInput : NavigationEventInput() {

    /* back */

    fun onBackStarted(event: NavigationEvent) {
        dispatchOnBackStarted(event)
    }

    fun onBackProgressed(event: NavigationEvent) {
        dispatchOnBackProgressed(event)
    }

    fun onBackCompleted() {
        dispatchOnBackCompleted()
    }

    fun onBackCancelled() {
        dispatchOnBackCancelled()
    }

    /* forward */

    fun onForwardStarted(event: NavigationEvent) {
        dispatchOnForwardStarted(event)
    }

    fun onForwardProgressed(event: NavigationEvent) {
        dispatchOnForwardProgressed(event)
    }

    fun onForwardCompleted() {
        dispatchOnForwardCompleted()
    }

    fun onForwardCancelled() {
        dispatchOnForwardCancelled()
    }
}
