package com.example.wifigetdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.wifigetdata.ui.theme.WifigetdataTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The main activity for the app
 * @see ComponentActivity
 * @see MainViewModel
 * @see Repository
 * @see AppDatabase
 * @see MainViewModelFactory
 * @constructor Create empty Main activity
 * @property database the database to use
 */
open class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "dataValues.db"
        ).build()
    }
    private lateinit var sharedViewModel : MainViewModel
    private lateinit var repository : Repository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                repository = Repository(database)
                sharedViewModel = ViewModelProvider(
                    this@MainActivity,
                    MainViewModelFactory(repository)
                )[MainViewModel::class.java]

            }

        }

        setContent {
            WifigetdataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    /**
                     * Navigation
                     * @see rememberNavController
                     * @see NavHost
                     */
                    val navController = rememberNavController()

                    Scaffold(
                        bottomBar = {
                            BottomAppBar {
                                val showMenu = remember { mutableStateOf(false) }
                                val navOptions = listOf(Routes.MAIN, Routes.IP_SCANNER, Routes.CALIBRATION, Routes.HOME)

                                IconButton(onClick = { showMenu.value = true }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Navigation Menu")
                                }

                                DropdownMenu(
                                    expanded = showMenu.value,
                                    onDismissRequest = { showMenu.value = false }
                                ) {
                                    /**
                                     * Dropdown menu
                                     * @see DropdownMenuItem
                                     */
                                    navOptions.forEach { route ->
                                        DropdownMenuItem(
                                            text = { Text(route.toString()) },
                                            onClick = {
                                                navController.navigate(route.toString())
                                                showMenu.value = false
                                            }
                                        )
                                    }
                                }
                            }
                                    },
                        content = { innerPadding ->
                            /**
                             * Main Column
                             * @see Column
                             * @see MainScreen
                             */
                            Column(modifier = Modifier.padding(innerPadding)){
                                NavHost(
                                    navController = navController,
                                    startDestination = Routes.MAIN.toString()
                                ) {
                                    composable(Routes.MAIN.toString()) {
                                        MainScreen(sharedViewModel, navController)
                                        sharedViewModel.screen = Routes.MAIN
                                    }
                                    composable(Routes.IP_SCANNER.toString()) {
                                        ScanScreen(
                                            sharedViewModel = sharedViewModel,
                                            applicationContext = this@MainActivity,
                                            navController = navController
                                        )
                                        sharedViewModel.screen = Routes.IP_SCANNER
                                    }
                                    composable(Routes.CALIBRATION.toString()) {
                                        CalibrationScreen(
                                            sharedViewModel = sharedViewModel,
                                            navController = navController
                                        )
                                        sharedViewModel.screen = Routes.CALIBRATION
                                    }
                                    composable(Routes.HOME.toString()) {
                                        HomeScreen(
                                            sharedViewModel = sharedViewModel,
                                            navController = navController,
                                            context = this@MainActivity
                                        )
                                        sharedViewModel.screen = Routes.HOME
                                    }
                                }
                            }
                    })
                    /**
                     * Launched effect
                     * @see LaunchedEffect
                     */
                    LaunchedEffect(Unit) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Default){
                                /**
                                 * Collect data
                                 * Updates data every time new value is emitted
                                 * @see DataValueDao
                                 * @see collect
                                 */
                                sharedViewModel.repository.dataValueDao.getAll().collect {
                                    if (it.isNotEmpty()){
                                        println("New Data ${it.last().voltage} V, ${it.last().concentration} ppm")
                                    } else {
                                        println("Data is empty")
                                    }
                                    when (sharedViewModel.screen) {
                                        Routes.CALIBRATION -> {
                                            if (it.size >= 2) {
                                                sharedViewModel.updateR2Score(it)
                                                if (it.size > 2) {
                                                    sharedViewModel.updateGradientIntercept(it)
                                                }
                                            }
                                        }
                                        Routes.HOME -> {
                                            println("Updated to ${it.last().voltage} V, ${it.last().concentration} ppm")
                                            sharedViewModel.allData.value = it
                                        }
                                        Routes.MAIN -> {}
                                        Routes.IP_SCANNER -> TODO()
                                    }
                                }
                            }
                        }
                    }
                    LaunchedEffect(sharedViewModel.screen) {
                        navController.navigate(sharedViewModel.screen.toString())
                    }
                }
            }
        }
    }
}

