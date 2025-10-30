package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class ComposeSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleTheme {
                EdgeToEdgeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EdgeToEdgeScreen() {
    val colors: List<Int> by remember {
        mutableStateOf(
            List(10) {
                listOf(
                    android.graphics.Color.BLACK,
                    android.graphics.Color.BLUE,
                    android.graphics.Color.CYAN,
                    android.graphics.Color.DKGRAY,
                    android.graphics.Color.GRAY,
                    android.graphics.Color.GREEN,
                    android.graphics.Color.LTGRAY,
                    android.graphics.Color.MAGENTA,
                    android.graphics.Color.RED,
                    android.graphics.Color.WHITE,
                    android.graphics.Color.YELLOW,
                )
            }.flatten()
        )
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Compose Sample") }) },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(count = 3),
            contentPadding = PaddingValues(horizontal = 16.dp) + paddingValues,
        ) {
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(Color(color)),
                )
            }
        }
    }
}

private operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    return PaddingValues(
        start = this.calculateLeftPadding(LayoutDirection.Ltr)
                + other.calculateLeftPadding(LayoutDirection.Ltr),
        top = this.calculateTopPadding()
                + other.calculateTopPadding(),
        end = this.calculateRightPadding(LayoutDirection.Ltr)
                + other.calculateRightPadding(LayoutDirection.Ltr),
        bottom = this.calculateBottomPadding()
                + other.calculateBottomPadding(),
    )
}

@Preview(showBackground = true)
@Composable
private fun EdgeToEdgeScreenPreview() {
    SampleTheme {
        EdgeToEdgeScreen()
    }
}
