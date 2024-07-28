package com.example.wifigetdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
                            withContext(Dispatchers.IO){
                                sharedViewModel.repository.dataValueDao.getAll().collect {
                                    println("New Data ${it}")
                                    when (sharedViewModel.screen) {
                                        Routes.CALIBRATION -> {
                                            if (it.size >= 2) {
                                                sharedViewModel.updateR2Score(it)
                                            }
                                            if (it.size > 2) {
                                                sharedViewModel.updateGradientIntercept()
                                                //if (sharedViewModel.r2score.value >= 0.9) {
                                                //    println("R2score is sufficient. Switching to main screen")
                                                //    sharedViewModel.screen = Routes.HOME
                                                //}
                                                if (it.size > 2) {
                                                }
                                            }
                                        }
                                        Routes.HOME -> {

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

