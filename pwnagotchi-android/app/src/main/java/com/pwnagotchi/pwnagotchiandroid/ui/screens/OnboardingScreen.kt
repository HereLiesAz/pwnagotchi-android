package com.pwnagotchi.pwnagotchiandroid.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    HorizontalPager(state = pagerState) { page ->
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
            2 -> OnboardingPage(title = "Let's Go!", description = "You're all set. Let's get started!") {
                Button(onClick = onOnboardingComplete) {
                    Text("Finish")
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(title: String, description: String, content: @Composable () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = title)
        Text(text = description)
        content()
    }
}