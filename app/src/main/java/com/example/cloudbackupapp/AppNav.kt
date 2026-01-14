package com.example.cloudbackupapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Animated, easily-modifiable screen navigation.
 *
 * Why this exists:
 * - Navigation libraries evolve and add/remove animation APIs.
 * - This app keeps a simple in-memory back stack and animates transitions via [AnimatedContent].
 *
 * Change animations in one place: see [AppMotion].
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNav() {
    val backStack = remember { mutableStateListOf<String>(Routes.Main) }

    var lastStackSize by remember { mutableIntStateOf(backStack.size) }
    var lastRoute by remember { mutableStateOf(backStack.last()) }

    val currentRoute = backStack.last()
    val isPop = backStack.size < lastStackSize

    // Update trackers after composition reads current state.
    lastStackSize = backStack.size
    lastRoute = currentRoute

    fun navigate(route: String) {
        backStack.add(route)
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            AppMotion.screenContentTransform(isPop = isPop)
        },
        label = "screen-transition"
    ) { route ->
        when {
            route == Routes.Main -> {
                MainScreen(
                    onOpen = { title ->
                        when (title.lowercase()) {
                            "photos" -> navigate(Routes.Photos)
                            "videos" -> navigate(Routes.Videos)
                            "messages" -> navigate(Routes.Sms)
                            "call logs" -> navigate(Routes.CallLogs)
                            "contacts" -> navigate(Routes.Contacts)
                            "storage" -> navigate(Routes.Storage)
                            "connect" -> navigate(Routes.Settings)
                            "schedule" -> navigate(Routes.Schedule)
                            "see all" -> navigate(Routes.Storage)
                            "passwords" -> navigate(Routes.Passwords)
                            else -> navigate(Routes.template(title))
                        }
                    }
                )
            }

            route == Routes.Settings -> SettingsScreen(onBack = { pop() })
            route == Routes.Storage -> StorageScreen(onBack = { pop() })
            route == Routes.Photos -> PhotosScreen(onBack = { pop() })
            route == Routes.Videos -> VideosScreen(onBack = { pop() })
            route == Routes.Sms -> SmsBackupsScreen(onBack = { pop() })
            route == Routes.CallLogs -> CallLogBackupsScreen(onBack = { pop() })
            route == Routes.Passwords -> PasswordsLauncherScreen(onBack = { pop() })
            route == Routes.Schedule -> BackupScheduleScreen(onBack = { pop() })
            route == Routes.Contacts -> ContactsScreen(onBack = { pop() })

            route.startsWith(Routes.TemplatePrefix) -> {
                val title = Routes.templateTitle(route)
                TemplateScreen(title = title, onBack = { pop() })
            }

            else -> TemplateScreen(title = route, onBack = { pop() })
        }
    }
}

private object Routes {
    const val Main = "main"
    const val Settings = "settings"
    const val Storage = "storage"
    const val Photos = "photos"
    const val Videos = "videos"
    const val Sms = "sms"
    const val CallLogs = "calllogs"
    const val Passwords = "passwords"
    const val Schedule = "schedule"
    const val Contacts = "contacts"

    const val TemplatePrefix = "template|"

    fun template(title: String): String = TemplatePrefix + encodeRouteArg(title)
    fun templateTitle(route: String): String =
        decodeRouteArg(route.removePrefix(TemplatePrefix))
}

/** Simple URL-ish encoding so titles with spaces work in routes. */
private fun encodeRouteArg(raw: String): String = java.net.URLEncoder.encode(raw, "UTF-8")
private fun decodeRouteArg(raw: String): String = java.net.URLDecoder.decode(raw, "UTF-8")
