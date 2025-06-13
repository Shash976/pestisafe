package com.example.pestisafe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PesticideSelectionScreen(sharedViewModel: MainViewModel, navController: NavController) {
    val pesticides = sharedViewModel.repository.pesticideDao.getAll().observeAsState(initial = emptyList())
    val isLoading = pesticides.value.isEmpty() // Check if the list is still loading
    val selectedPesticide = remember { mutableStateOf<Pesticide?>(null) }
    val expanded = remember { mutableStateOf(false) }

    // Debugging logs
    LaunchedEffect(pesticides.value) {
        println("Pesticides observed: ${pesticides.value}")
    }

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Select a Pesticide")

        if (isLoading) {
            Text("Loading pesticides...")
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { expanded.value = true },
                ) {
                    Text(selectedPesticide.value?.name ?: "Choose a pesticide")
                }
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    pesticides.value.forEach { pesticide ->
                        DropdownMenuItem(
                            text = { Text(text = pesticide.name) },
                            onClick = {
                                selectedPesticide.value = pesticide
                                expanded.value = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    selectedPesticide.value?.let {
                        sharedViewModel.pesticides = listOf(it)
                        println("Selected pesticide: ${it.name}")
                        navController.navigate(Routes.IP_SCANNER.toString())
                    }
                },
                enabled = selectedPesticide.value != null // Disable button if no selection
            ) {
                Text("Confirm")
            }
        }
    }
}

