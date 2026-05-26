import android.os.Build
import org.mindrot.jbcrypt.BCrypt
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.pestisafe.MainViewModel
import com.example.pestisafe.R
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
            // Add Image here
            Image(
                painter = painterResource(id = R.drawable.heading), // Replace with your image resource
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(500.dp) // Set desired size
                    .padding(16.dp)
            )

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

                val forgotPasswordCheck = remember { mutableStateOf(false) }
                val dobEntered = remember { mutableStateOf(false)         }

                if (forgotPasswordCheck.value) {
                    DatePickerField(
                        selectedDate = dob,
                        onDateChange = { dob = it; dobEntered.value = true },
                        label = "Date of Birth"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            forgotPasswordCoroutine.launch {
                                val user = sharedViewModel.repository.userDao.getUser(username = username)
                                if (user == null) {
                                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                                } else if (user.dob.equals(dob?.toString(), ignoreCase = true)) {                                    // DOB matches, proceed to reset password
                                    // Show reset password dialog or navigate to a reset password screen
                                    forgotPassword = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Incorrect Date of Birth",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Verify Identity")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = {
                    forgotPasswordCheck.value = true
                }) {
                    Text("Forgot Password?", fontSize = 14.sp)
                }
                if (forgotPassword) {
                    ShowResetPasswordDialog(sharedViewModel, username, navController, onDismiss = { forgotPassword = false })
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
                            val user = sharedViewModel.repository.userDao.getUser(username = username)
                            if (user == null) {
                                // Handle user not found
                                Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                            } else if (BCrypt.checkpw(password, user.password)){
                                sharedViewModel.user = user
                                navController.navigate(Routes.MAIN.toString())
                            } else {
                                // Handle incorrect password
                                Toast.makeText(context, "Incorrect Password", Toast.LENGTH_SHORT).show()
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
                                if (dob == null) {
                                    Toast.makeText(context, "Please select a date of birth", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                try {
                                    sharedViewModel.repository.userDao.insert(User(name = name, username = username, password = BCrypt.hashpw(password, BCrypt.gensalt()), email = email, dob = dob.toString()))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                sharedViewModel.user = sharedViewModel.repository.userDao.getUser(username = username)
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
    val context = LocalContext.current

    OutlinedTextField(
        value = selectedDate?.toString() ?: "",
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                android.app.DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        onDateChange(LocalDate.of(selectedYear, selectedMonth + 1, selectedDay))
                    },
                    year,
                    month,
                    day
                ).show()
            }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            }
        }
    )
}

@Composable
fun ShowResetPasswordDialog(sharedViewModel: MainViewModel, username: String, navController: NavController, onDismiss: () -> Unit) {
    var newPassword by remember { mutableStateOf("") }
    val forgotPasswordCoroutine = rememberCoroutineScope()
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss() },
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
                    sharedViewModel.repository.userDao.resetPassword(username, BCrypt.hashpw(newPassword, BCrypt.gensalt()))
                    val updatedUser = sharedViewModel.repository.userDao.getUser(username = username)
                    if (updatedUser == null) {
                        Toast.makeText(context, "User not found after password reset", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    sharedViewModel.user = updatedUser
                    Toast.makeText(context, "Password reset successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.MAIN.toString())
                }
            }) {
                Text("Reset")

            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}