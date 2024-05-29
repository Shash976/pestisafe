package com.example.wifigetdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
            //val databaseDataValues by database.dataValueDao().getAll().observeAsState(emptyList())
            WifigetdataTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Routes.MAIN.toString()){
                        composable(Routes.MAIN.toString()){
                            MainScreen(sharedViewModel, navController)
                        }
                        composable(Routes.IP_SCANNER.toString()){
                            ScanScreen(sharedViewModel = sharedViewModel,
                                applicationContext = this@MainActivity,
                                navController = navController
                            )
                        }
                        composable(Routes.CALIBRATION.toString()){
                            CalibrationScreen(sharedViewModel = sharedViewModel, navController=navController)
                        }
                        composable(Routes.HOME.toString()){
                            HomeScreen(sharedViewModel = sharedViewModel, navController = navController, context = this@MainActivity)
                        }
                    }
                }
            }
        }
    }
}

