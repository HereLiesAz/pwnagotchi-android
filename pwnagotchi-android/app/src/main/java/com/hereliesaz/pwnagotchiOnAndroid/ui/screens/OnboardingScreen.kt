package com.hereliesaz.pwnagotchiOnAndroid.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(title = "Welcome to Pwnagotchi!", description = "The ultimate companion for your Pwnagotchi device.")
                1 -> {
                    val postNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        null
                    }
                    OnboardingPage(title = "Permissions", description = "We need some permissions to work our magic.") {
                        if (postNotificationPermission != null) {
                            if (!postNotificationPermission.status.isGranted) {
                                Button(onClick = { postNotificationPermission.launchPermissionRequest() }) {
                                    Text("Grant Notification Permission")
                                }
                            } else {
                                Text("Notification permission granted!")
                            }
                        }
                    }
                }
                2 -> OnboardingPage(title = "Let's Go!", description = "You're all set. Let's get started!")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onOnboardingComplete) {
                Text("Skip")
            }

            // Simple dot indicator
            Row {
                for (i in 0 until pagerState.pageCount) {
                    val color = if (pagerState.currentPage == i) Color.Black else Color.Gray
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .padding(2.dp)
                            .background(color, shape = CircleShape)
                    )
                }
            }

            Button(onClick = {
                if (pagerState.currentPage < pagerState.pageCount - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onOnboardingComplete()
                }
            }) {
                Text(if (pagerState.currentPage < pagerState.pageCount - 1) "Next" else "Finish")
            }
        }
    }
}

@Composable
fun OnboardingPage(title: String, description: String, content: @Composable () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = description, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        content()
    }
}
