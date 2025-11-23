package com.example.profile.impl

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.common.EntryProviderInstaller
import com.example.common.Navigator
import com.example.profile.Info
import com.example.profile.Profile
import dagger.Module
import dagger.Provides
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object ProfileModule {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller = {
        entry<Profile>(
//            metadata = ListDetailSceneStrategy.extraPane(),
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = "profile",
                detailPlaceholder = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Placeholder")
                    }
                },
            )
        ) { key ->
            val viewModel = hiltViewModel<ProfileViewModel, ProfileViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(key)
                }
            )
            ProfileScreen(
                viewModel,
                key,
                onProfileClicked = { navigator.goTo(Profile(id = "2")) },
                onInfoClicked = { navigator.goTo(Info(id = "1")) },
            )
        }
        entry<Info>(
            metadata = ListDetailSceneStrategy.detailPane("profile"),
        ) { key ->
            val viewModel = hiltViewModel<InfoViewModel, InfoViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(key)
                }
            )
            InfoScreen(
                viewModel,
                key,
                onInfoClicked = { navigator.goTo(Info(id = key.id + "1")) },
            )
        }
    }
}

@Composable
private fun ProfileScreen(
    viewModel: ProfileViewModel,
    key: Profile,
    onProfileClicked: () -> Unit,
    onInfoClicked: () -> Unit,
) {
    val profileColor = MaterialTheme.colorScheme.surfaceVariant
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profileColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Profile Screen $key - $viewModel",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onProfileClicked) {
            Text("View Profile")
        }
        Button(onClick = onInfoClicked) {
            Text("View Info")
        }
    }
}

@HiltViewModel(assistedFactory = ProfileViewModel.Factory::class)
class ProfileViewModel @AssistedInject constructor(
    @Assisted val profile: Profile
) : ViewModel() {

    init {
        Log.d("ProfileViewModel", "init: ${profile.id} - ${profile.hashCode()} - $profile")
    }

    @AssistedFactory
    interface Factory {
        fun create(profile: Profile): ProfileViewModel
    }
}

@Composable
private fun InfoScreen(
    viewModel: InfoViewModel,
    key: Info,
    onInfoClicked: () -> Unit,
) {
    val profileColor = MaterialTheme.colorScheme.surfaceDim
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(profileColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Info Screen $key",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onInfoClicked) {
            Text("View Info")
        }
    }
}

@HiltViewModel(assistedFactory = InfoViewModel.Factory::class)
class InfoViewModel @AssistedInject constructor(
    @Assisted val info: Info
) : ViewModel() {

    init {
        Log.d("InfoViewModel", "init: ${info.id} - ${info.hashCode()} - $info")
    }

    @AssistedFactory
    interface Factory {
        fun create(info: Info): InfoViewModel
    }
}
