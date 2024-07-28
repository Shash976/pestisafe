package com.example.wifigetdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.wifigetdata.ui.theme.WifigetdataTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


open class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "dataValues.db"
        ).build()
    }
   // val sharedViewModel = ViewModelProvider((applicationContext as AppViewModel))[MainViewModel::class.java]
    private lateinit var sharedViewModel : MainViewModel
    private lateinit var repository : Repository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                repository = Repository(database.dataValueDao())
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
//                                Row {
//                                    Button(onClick = {
//                                        navController.navigate(Routes.MAIN.toString())
//                                    }) {
//                                        Text("Main")
//                                    }
//                                    Button(onClick = {
//                                        navController.navigate(Routes.IP_SCANNER.toString())
//                                    }) {
//                                        Text("Scan")
//                                    }
//                                    Button(onClick = {
//                                        navController.navigate(Routes.CALIBRATION.toString())
//                                    }) {
//                                        Text("Calibration")
//                                    }
//                                    Button(onClick = {
//                                        navController.navigate(Routes.HOME.toString())
//                                    }) {
//                                        Text("Home")
//                                    }
//                                }
                            }
                                    },
                        content = { innerPadding ->
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
                    LaunchedEffect(Unit) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Default){
                                sharedViewModel.repository.dataValueDao.getAll().collect {
                                    if (it.isNotEmpty()){
                                        println("New Data ${it.last().voltage} V, ${it.last().concentration} μm")
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
                                            println("Updated to ${it.last().voltage} V, ${it.last().concentration} μm")
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

