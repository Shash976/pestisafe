package com.example.wifigetdata

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class AppViewModel : Application(), ViewModelStoreOwner {

    override val viewModelStore = ViewModelStore()

    // Override getViewModelStore from ViewModelStoreOwner

    private lateinit var sharedViewModel: MainViewModel

    override fun onCreate() {
        super.onCreate()
        sharedViewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }
}

class MainViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}