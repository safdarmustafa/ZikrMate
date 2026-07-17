package com.falahpro.app

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.falahpro.app.auth.LoginScreen
import com.falahpro.app.dua.DuaScreen
import com.falahpro.app.profile.ProfileScreen
import com.falahpro.app.prayer.PrayerDiagnosticsScreen
import com.falahpro.app.prayer.PrayerTrackerScreen
import com.falahpro.app.prayer.rememberPrayerViewModel
import com.falahpro.app.prayer.rememberPrayerVisualEffects
import com.falahpro.app.qibla.QiblaScreen
import com.falahpro.app.tasbih.TasbihScreen
import com.falahpro.app.ui.theme.SplashScreenJcTheme
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle

class FalahPro : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        setContent {
            SplashScreenJcTheme {
                AuthGate()
            }
        }
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A120F)
            ) {
                NavigationBarItem(
                    selected = currentRoute == "tasbih",
                    onClick = { navigateToTab("tasbih") },
                    icon = { Text("📿") },
                    label = { Text("Tasbih") }
                )
                NavigationBarItem(
                    selected = currentRoute == "prayer",
                    onClick = { navigateToTab("prayer") },
                    icon = { Text("🕌") },
                    label = { Text("Prayer") }
                )
                NavigationBarItem(
                    selected = currentRoute == "qibla",
                    onClick = { navigateToTab("qibla") },
                    icon = { Text("🧭") },
                    label = { Text("Qibla") }
                )
                NavigationBarItem(
                    selected = currentRoute == "dua",
                    onClick = { navigateToTab("dua") },
                    icon = { Text("📖") },
                    label = { Text("Dua") }
                )
                NavigationBarItem(
                    selected = currentRoute == "profile",
                    onClick = { navigateToTab("profile") },
                    icon = { Text("👤") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->

        val prayerViewModel = rememberPrayerViewModel()
        val prayerVisualEffects = rememberPrayerVisualEffects()

        NavHost(
            navController = navController,
            startDestination = "tasbih",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("tasbih") {
                TasbihScreen()
            }

            composable("prayer") {
                PrayerTrackerScreen(
                    viewModel = prayerViewModel,
                    visualEffects = prayerVisualEffects
                )
            }

            if (BuildConfig.DEBUG) {
                composable("prayer_diagnostics") {
                    PrayerDiagnosticsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable("qibla") {
                QiblaScreen(
                    onBack = { navigateToTab("tasbih") }
                )
            }

            composable("dua") {
                DuaScreen()
            }

            composable("profile") {
                ProfileScreen(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("tasbih") {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AuthGate() {

    val auth = FirebaseAuth.getInstance()
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (isLoggedIn) {
        AppNavigation()
    } else {
        LoginScreen(
            onLoginSuccess = {
                isLoggedIn = true
            }
        )
    }
}
