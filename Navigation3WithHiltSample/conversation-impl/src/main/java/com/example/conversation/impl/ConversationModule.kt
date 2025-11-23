package com.example.conversation.impl

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation3.scene.DialogSceneStrategy
import com.example.common.BottomSheetSceneStrategy
import com.example.common.EntryProviderInstaller
import com.example.common.Navigator
import com.example.conversation.BottomSheet
import com.example.conversation.ConversationDetail
import com.example.conversation.ConversationList
import com.example.conversation.Dialog
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
import javax.inject.Inject

@Module
@InstallIn(ActivityRetainedComponent::class)
object ConversationModule {

    @OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
    @IntoSet
    @Provides
    fun provideEntryProviderInstaller(navigator: Navigator): EntryProviderInstaller =
        {
            entry<ConversationList>(
                metadata = ListDetailSceneStrategy.listPane(
                    sceneKey = "conversation",
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
            ) {
                val viewModel = hiltViewModel<ConversationListViewModel>()
                ConversationListScreen(
                    viewModel,
                    onConversationClicked = { conversationDetail ->
                        navigator.goTo(conversationDetail)
                    }
                )
            }
            entry<ConversationDetail>(
                metadata = ListDetailSceneStrategy.detailPane("conversation")
            ) { key ->
                val viewModel = hiltViewModel<ConversationDetailViewModel, ConversationDetailViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(key)
                    }
                )
                ConversationDetailScreen(
                    viewModel,
                    conversationDetail = key,
                    onProfileClicked = { navigator.goTo(Profile(id = "1")) },
                    onDialogClicked = { navigator.goTo(Dialog(id = "dialog")) },
                    onBottomSheetClicked = { navigator.goTo(BottomSheet(id = "bottom sheet")) },
                )
            }
            entry<Dialog>(
                metadata = DialogSceneStrategy.dialog()
            ) { key ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(48.dp))
                        .background(Color.Green),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = key.id)
                }
            }
            entry<BottomSheet>(
                metadata = BottomSheetSceneStrategy.bottomSheet()
            ) { key ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Green),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = key.id)
                }
            }
        }
}

@Composable
private fun ConversationListScreen(
    viewModel: ConversationListViewModel,
    onConversationClicked: (ConversationDetail) -> Unit
) {
    Scaffold { paddingValues ->
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
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
}

@HiltViewModel
class ConversationListViewModel @Inject constructor(
) : ViewModel() {

    init {
        Log.d("ConversationDetailViewModel", "init: ${hashCode()}")
    }
}


@Composable
private fun ConversationDetailScreen(
    viewModel: ConversationDetailViewModel,
    conversationDetail: ConversationDetail,
    onProfileClicked: () -> Unit,
    onDialogClicked: () -> Unit,
    onBottomSheetClicked: () -> Unit,
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
        Button(onClick = onDialogClicked) {
            Text("View Dialog")
        }
        Button(onClick = onBottomSheetClicked) {
            Text("View Bottom Sheet")
        }
    }
}

private val ConversationDetail.color: Color
    get() = colors[id % colors.size]

@HiltViewModel(assistedFactory = ConversationDetailViewModel.Factory::class)
class ConversationDetailViewModel @AssistedInject constructor(
    @Assisted val collectionDetail: ConversationDetail
) : ViewModel() {

    init {
        Log.d("ConversationDetailViewModel", "init: ${collectionDetail.id} - ${collectionDetail.hashCode()} - $collectionDetail")
    }

    @AssistedFactory
    interface Factory {
        fun create(collectionDetail: ConversationDetail): ConversationDetailViewModel
    }
}
