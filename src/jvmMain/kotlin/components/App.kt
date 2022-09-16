package tk.romanaugsto.salariumauto.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import tk.romanaugsto.salariumauto.Main
import utils.Scheduler
import webscrapper.Webscrapper

class App {
    object Statuses {
        const val incorrectCreds = "Password or Email is incorrect"
        const val isLoadingService = "Spinning up chrome background service"
        const val scheduling = "Scheduling tasks"
        const val cancelled = "Task was cancelled"
        const val loggingIn = "Logging in at -time-"
        const val incorrectDate = "Time format must be 24 time format and must be at a later time"
        const val loggedIn = "Logged in at -time-"
    }

    private fun getIntervalTime(interval: Long): String {
        val hours = interval / 3600
        val minutes = (interval % 3600L).floorDiv(60L)
        val seconds = interval % 60
        val hoursDisplay = if (hours == 0L) {
            ""
        } else {
            if (hours.toString().length == 2) "${hours}:" else "0$hours:"
        }
        val minuteDisplay = if (minutes == 0L && hours == 0L) {
            ""
        } else {
            if (minutes.toString().length == 2) "${minutes}:" else "0$minutes:"
        }

        val secondsDisplay = if (seconds.toString().length == 2) seconds else "0$seconds"

        return "${hoursDisplay}${minuteDisplay}${secondsDisplay}"
    }

    @Composable
    fun mainWindow() {
        val currentTime = DateTime.now().plusMinutes(2).toString("HH:mm:ss")
        var email by remember { mutableStateOf(Main.settings.cachedEmail) }
        var password by remember { mutableStateOf(Main.settings.cachedPassword) }
        var loginTime by remember { mutableStateOf(currentTime) }
        var intervalTime by remember { mutableStateOf(0L) }
        var isLoading by remember { mutableStateOf(false) }
        var status by remember { mutableStateOf("") }
        var statusColor by remember { mutableStateOf(Color.White) }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }


        MaterialTheme {
            Row(
                Modifier.fillMaxHeight().background(
                    color = Color(0x0F172A).copy(alpha = 1f)
                ),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(50.dp)
                ) {
                    Row(Modifier.shadow(20.dp)) {
                        Row(
                            Modifier
                                .height(80.dp)
                                .background(color = Color(0x334155).copy(alpha = 1f))
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Salarium Auto Login",
                                Modifier.padding(15.dp),
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                fontSize = 30.sp
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,

                        ) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)

                        ) {
                            if (intervalTime != 0L) {
                                Row {
                                    Text(
                                        modifier = Modifier.shadow(20.dp),
                                        text = "Logging In: ${getIntervalTime(intervalTime)}",
                                        color = Color.White,
                                        fontSize = 30.sp
                                    )
                                }
                            } else {
                                Row {
                                    Text(
                                        modifier = Modifier.shadow(20.dp),
                                        fontSize = 30.sp,
                                        color = Color.White,
                                        text = "Enter Login Time"
                                    )
                                }
                            }
                            if (status.isNotBlank()) {
                                Row {
                                    Text(
                                        fontSize = 15.sp,
                                        color = statusColor,
                                        text = status
                                    )
                                }
                            }
                            Row {
                                Column(
                                    Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {

                                    Row {

                                        OutlinedTextField(
                                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                                textColor = Color.White,
                                                unfocusedBorderColor = Color.White,
                                                placeholderColor = Color.White,
                                                unfocusedLabelColor = Color.White
                                            ),
                                            value = email,
                                            onValueChange = { email = it },
                                            enabled = !isLoading && intervalTime == 0L,
                                            label = { Text("Email") },
                                            singleLine = true

                                        )
                                    }
                                    Row {
                                        OutlinedTextField(
                                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                                textColor = Color.White,
                                                unfocusedBorderColor = Color.White,
                                                placeholderColor = Color.White,
                                                unfocusedLabelColor = Color.White
                                            ),
                                            value = password,
                                            onValueChange = { password = it },
                                            enabled = !isLoading && intervalTime == 0L,
                                            label = { Text("Password") },
                                            singleLine = true,
                                            visualTransformation = if (passwordVisible && !isLoading && intervalTime == 0L) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            trailingIcon = {
                                                val image = if (passwordVisible) Icons.Filled.Info
                                                else Icons.Outlined.Info
                                                val description =
                                                    if (passwordVisible) "Hide password" else "Show password"

                                                IconButton(
                                                    onClick = { passwordVisible = !passwordVisible },
                                                    enabled = !isLoading && intervalTime == 0L
                                                ) {
                                                    Icon(
                                                        imageVector = image,
                                                        contentDescription = description,
                                                        tint = if (!isLoading && intervalTime == 0L) Color.White else Color.Gray
                                                    )
                                                }

                                            }
                                        )
                                    }

                                    Row {
                                        OutlinedTextField(
                                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                                textColor = Color.White,
                                                unfocusedBorderColor = Color.White,
                                                placeholderColor = Color.White,
                                                unfocusedLabelColor = Color.White

                                            ),
                                            value = loginTime,
                                            enabled = !isLoading && intervalTime == 0L,
                                            onValueChange = {
                                                loginTime = it
                                            },
                                            label = { Text("Login Time (HH:MM:SS)") }
                                        )

                                    }
                                }
                            }

                            if (isLoading) {
                                Row {
                                    CircularProgressIndicator()
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Button(
                                    onClick = {
                                        Main.mainCoroutine.launch {
                                            Main.driver.close()
                                            intervalTime = 0
                                            statusColor = Color.White
                                            status = Statuses.cancelled
                                            delay(2000L)
                                            status = ""
                                            return@launch
                                        }

                                    },
                                    enabled = intervalTime != 0L
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    enabled = !isLoading && intervalTime == 0L,
                                    onClick = {
                                        if (email.isEmpty() || password.isEmpty()) {
                                            status = "Credentials are empty."
                                            statusColor = Color.Red
                                            Main.mainCoroutine.launch {
                                                delay(2000)
                                                status = ""
                                                return@launch
                                            }
                                            return@Button
                                        }
                                        if (!loginTime.matches(Regex("(?:[01]\\d|2[0123]):[012345]\\d:[012345]\\d"))) {
                                            status = Statuses.incorrectDate
                                            statusColor = Color.Red
                                            Main.mainCoroutine.launch {
                                                delay(2000)
                                                status = ""
                                                return@launch
                                            }
                                            return@Button
                                        }
                                        statusColor = Color.White
                                        status = ""
                                        isLoading = true
                                        Main.mainCoroutine.launch {
                                            try {
                                                if (!Webscrapper(email, password).login()) {
                                                    status = Statuses.incorrectCreds
                                                    statusColor = Color.Red
                                                    isLoading = false
                                                    return@launch
                                                }
                                            } catch (e: Throwable) {
                                                isLoading = false
                                                status = "Unknown Error Occurred"
                                                statusColor = Color.Red
                                                e.printStackTrace()
                                                return@launch
                                            }
                                            status = Statuses.isLoadingService
                                            Main.settings.cachedEmail = email
                                            Main.settings.cachedPassword = password
                                            Main.config.save(Main.settings)
                                            val privateTimer = Scheduler(email, password).start(loginTime)
                                            Main.driver = privateTimer.driver
                                            isLoading = false
                                            status = Statuses.scheduling
                                            intervalTime = privateTimer.interval

                                            var previous = System.currentTimeMillis()

                                            Main.mainCoroutine.launch {
                                                while (intervalTime > 0) {
                                                    delay(1000L)
                                                    intervalTime -= System.currentTimeMillis() / 1000 - previous / 1000
                                                    previous = System.currentTimeMillis()
                                                }

                                                intervalTime = 0
                                                privateTimer.timer.cancel()
                                                if (status != Statuses.cancelled) {
                                                    status = Statuses.loggedIn
                                                        .replace("-time-", loginTime)
                                                }
                                            }
                                            status = Statuses.loggingIn.replace("-time-", loginTime)
                                        }

                                    }
                                )
                                {
                                    Text("Login")
                                }
                            }
                        }
                    }
                    Row(
                        Modifier
                            .height(80.dp)
                            .background(color = Color(0x334155).copy(alpha = 1f))
                            .fillMaxWidth().padding(horizontal = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "© 2022 Roman Augusto",
                            color = Color.White
                        )
                        Text(
                            "Salarium Auto Scheduler V2",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}