package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

@Stable
private sealed class MainItem(val name: String) {
    data object View : MainItem(name = "View")
    data object Compose : MainItem(name = "Compose")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleTheme {
                MainScreen(
                    items = listOf(MainItem.View, MainItem.Compose),
                    onItemClick = {
                        val intent = when (it) {
                            MainItem.View -> Intent(this, ViewSampleActivity::class.java)
                            MainItem.Compose -> Intent(this, ComposeSampleActivity::class.java)
                        }
                        startActivity(intent)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    items: List<MainItem>,
    onItemClick: (MainItem) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("NavigationEvent Sample") }) },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            items.forEach { item ->
                Text(
                    text = item.name,
                    modifier = Modifier
                        .clickable { onItemClick(item) }
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun MainScreenPreview() {
    SampleTheme {
        MainScreen(
            items = listOf(MainItem.View, MainItem.Compose),
            onItemClick = {},
        )
    }
}
