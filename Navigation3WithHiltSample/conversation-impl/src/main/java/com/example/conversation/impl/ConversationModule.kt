package com.example.conversation.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.common.EntryProviderInstaller
import com.example.common.Navigator
import com.example.conversation.ConversationDetail
import com.example.conversation.ConversationList
import com.example.profile.Profile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object ConversationModule {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller =
        {
            entry<ConversationList>(
                metadata = ListDetailSceneStrategy.listPane()
            ) {
                ConversationListScreen(
                    onConversationClicked = { conversationDetail ->
                        navigator.goTo(conversationDetail)
                    }
                )
            }
            entry<ConversationDetail>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                ConversationDetailScreen(key) { navigator.goTo(Profile) }
            }
        }
}

@Composable
private fun ConversationListScreen(
    onConversationClicked: (ConversationDetail) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(10) { index ->
            val conversationId = index + 1
            val conversationDetail = ConversationDetail(conversationId)
            val backgroundColor = conversationDetail.color
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onConversationClicked(conversationDetail) }),
                headlineContent = {
                    Text(
                        text = "Conversation $conversationId",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = backgroundColor // Set container color directly
                )
            )
        }
    }
}

@Composable
private fun ConversationDetailScreen(
    conversationDetail: ConversationDetail,
    onProfileClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(conversationDetail.color)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Conversation Detail Screen: ${conversationDetail.id}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onProfileClicked) {
            Text("View Profile")
        }
    }
}

private val ConversationDetail.color: Color
    get() = colors[id % colors.size]
