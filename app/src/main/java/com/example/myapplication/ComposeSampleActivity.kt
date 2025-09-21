package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.photopicker.compose.EmbeddedPhotoPicker
import androidx.photopicker.compose.ExperimentalPhotoPickerComposeApi
import androidx.photopicker.compose.rememberEmbeddedPhotoPickerState
import coil3.compose.AsyncImage

class ComposeSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleTheme {
                PhotoPickerScreen()
            }
        }
    }
}

@OptIn(ExperimentalPhotoPickerComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PhotoPickerScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Compose Sample") }) },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        PhotoPickerContent(modifier = Modifier.padding(paddingValues))
    }
}

@OptIn(ExperimentalPhotoPickerComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PhotoPickerContent(
    modifier: Modifier = Modifier,
) {
    var showPhotoPicker: Boolean by remember { mutableStateOf(false) }
    var attachments: Set<Uri> by remember { mutableStateOf(emptySet()) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showPhotoPicker) {
                Button(
                    onClick = {
                        showPhotoPicker = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Hide Embedded Photo Picker")
                }
            } else {
                Button(
                    onClick = {
                        showPhotoPicker = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Show Embedded Photo Picker")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Attachments count: ${attachments.size}")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(attachments.toList()) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }
        }

        if (showPhotoPicker) {
            val pickerState = rememberEmbeddedPhotoPickerState(
                initialMediaSelection = attachments,
                onUriPermissionGranted = { uris ->
                    attachments += uris
                },
                onUriPermissionRevoked = { uris ->
                    attachments -= uris
                },
                onSelectionComplete = {
                    // Hide the embedded photo picker as the user is done with the
                    // photo/video selection
                    showPhotoPicker = false
                },
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Embedded Photo Picker",
                    )

                    EmbeddedPhotoPicker(
                        state = pickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoPickerScreenPreview() {
    SampleTheme {
        PhotoPickerScreen()
    }
}
