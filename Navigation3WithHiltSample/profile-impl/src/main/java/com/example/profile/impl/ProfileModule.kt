package com.example.profile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.example.common.EntryProviderInstaller
import com.example.common.Navigator
import com.example.profile.Info
import com.example.profile.Profile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
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
            metadata = ListDetailSceneStrategy.listPane("profile"),
        ) { key ->
            ProfileScreen(
                key,
                onProfileClicked = { navigator.goTo(Profile(id = "2")) },
                onInfoClicked = { navigator.goTo(Info(id = "1")) },
            )
        }
        entry<Info>(
            metadata = ListDetailSceneStrategy.detailPane("profile"),
        ) { key ->
            InfoScreen(
                key,
                onInfoClicked = { navigator.goTo(Info(id = key.id + "1")) },
            )
        }
    }
}

@Composable
private fun ProfileScreen(
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
            text = "Profile Screen $key",
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

@Composable
private fun InfoScreen(
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
