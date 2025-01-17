import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.pestisafe.MainViewModel
import com.example.pestisafe.Routes
import com.example.pestisafe.User
import kotlinx.coroutines.launch
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoginSignUpScreen(sharedViewModel :MainViewModel, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf<LocalDate?>(null) } // Date of Birth field
    var showPassword by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) } // Toggle between Login and Sign-Up

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLogin) "Login" else "Sign Up",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!isLogin) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                DatePickerField(
                    selectedDate = dob,
                    onDateChange = { dob = it },
                    label = "Date of Birth"
                )



                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Hide Password" else "Show Password"
                        )
                    }
                },
                singleLine = true
            )

            if (isLogin) {
                Spacer(modifier = Modifier.height(8.dp))
                val forgotPasswordCoroutine = rememberCoroutineScope()
                val context = LocalContext.current
                var forgotPassword by remember { mutableStateOf(false) }

                if (forgotPassword){
                    DatePickerField(
                        selectedDate = dob,
                        onDateChange = { dob = it },
                        label = "Date of Birth"
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = {
                    forgotPasswordCoroutine.launch {
                        try {
                            val user = sharedViewModel.repository.userDao.getUser(username = username)
                            if (user.dob.uppercase().strip() == dob.toString().uppercase()) {
                                // DOB matches, proceed to reset password
                                // Show reset password dialog or navigate to a reset password screen
                                forgotPassword = true

                            } else {
                                Toast.makeText(context, "Incorrect Date of Birth", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            // Handle user not found
                            // Show error message
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Forgot Password?", fontSize = 14.sp)
                }
                if (forgotPassword) {
                    ShowResetPasswordDialog(sharedViewModel, username)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            val getUsernameCoroutine = rememberCoroutineScope()
            val context = LocalContext.current
            Button(
                onClick = {
                    // Handle Login or Sign-Up logic here
                    if (isLogin){
                        getUsernameCoroutine.launch{
                            try {
                                val user = sharedViewModel.repository.userDao.getUser(username = username)
                                if (user.password == password){
                                    sharedViewModel.user = user
                                    navController.navigate(Routes.MAIN.toString())
                                } else {
                                    // Handle incorrect password
                                    // Show error message
                                    Toast.makeText(context, "Incorrect Password", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                // Handle user not found
                                // Show error message
                                Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else {
                        getUsernameCoroutine.launch{
                            val user = sharedViewModel.repository.userDao.getUser(username = username)
                            if (user != null) {
                                Toast.makeText(
                                    context,
                                    "Username already exists",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else{
                                sharedViewModel.repository.userDao.insert(User(name = name, username = username, password = password, email = email, dob = dob.toString()))
                                sharedViewModel.user = sharedViewModel.repository.userDao.getUser(username = username)
                                println("User: ${sharedViewModel.user!!.username}, Logging in")
                                navController.navigate(Routes.MAIN.toString())
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLogin) "Login" else "Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))


            TextButton(onClick = { isLogin = !isLogin }) {
                Text(
                    text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(selectedDate: LocalDate?, onDateChange: (LocalDate) -> Unit, label: String) {
    var isDialogOpen by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate?.toString() ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { isDialogOpen = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            }
        }
    )

    if (isDialogOpen) {
        val context = LocalContext.current
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        android.app.DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                onDateChange(LocalDate.of(selectedYear, selectedMonth + 1, selectedDay))
                isDialogOpen = false
            },
            year,
            month,
            day
        ).show()
    }
}

@Composable
fun ShowResetPasswordDialog(sharedViewModel: MainViewModel, username: String) {
    var newPassword by remember { mutableStateOf("") }
    val forgotPasswordCoroutine = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Reset Password") },
        text = {
            Column {
                Text("Enter your new password")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                forgotPasswordCoroutine.launch{
                    sharedViewModel.repository.userDao.resetPassword(username, newPassword)
                }
            }) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = { }) {
                Text("Cancel")
            }
        }
    )
}
