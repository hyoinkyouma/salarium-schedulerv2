// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.common.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.openqa.selenium.chrome.ChromeDriver

import utils.Scheduler
import webscrapper.Webscrapper


class Main {
    object Statuses {
        const val incorrectCreds = "Password or Email is incorrect"
        const val isLoadingService = "Spinning up chrome background service"
        const val scheduling = "Scheduling tasks"
        const val cancelled = "Task was cancelled"
        const val loggingIn = "Logging in at -time-"
        const val incorrectDate = "Incorrect time format must be 24 time format"
        const val loggedIn = "Logged in at -time-"
    }

    companion object {
        private lateinit var driver: ChromeDriver
        private val mainCoroutine = CoroutineScope(Job())
        private fun getIntervalTime(interval: Long): String {
            val hours = interval / 3600
            val minutes = (interval % 3600L).floorDiv(60L)
            val seconds = interval % 60
            return "${hours}:${minutes}:${seconds}"
        }

        @Preview
        @Composable
        fun app() {
            val currentTime = DateTime.now().plusMinutes(2).toString("HH:mm:ss")
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var loginTime by remember { mutableStateOf(currentTime) }
            var intervalTime by remember { mutableStateOf(0L) }
            var isLoading by remember { mutableStateOf(false) }
            var status by remember { mutableStateOf("") }
            var statusColor by remember { mutableStateOf(Color.White) }

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
                        Row {
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
                                            text = "Logging In: ${getIntervalTime(intervalTime)}",
                                            color = Color.White,
                                            fontSize = 30.sp
                                        )
                                    }
                                } else {
                                    Row {
                                        Text(
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
                                                label = { Text("Email") }

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
                                                label = { Text("Password") }

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
                                            mainCoroutine.launch {
                                                driver.close()
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
                                            statusColor = Color.White
                                            status = ""
                                            isLoading = true
                                            mainCoroutine.launch {
                                                val datimeCompare =
                                                    DateTimeFormat.forPattern("HH:mm:ss").parseLocalTime(loginTime)
                                                        .toDateTimeToday().toInstant().millis
                                                if (datimeCompare <= DateTime.now().toInstant().millis) {
                                                    statusColor = Color.Red
                                                    status = Statuses.incorrectDate
                                                    delay(2000L)
                                                    isLoading = false
                                                    return@launch
                                                }
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
                                                val privateTimer = Scheduler(email, password).start(loginTime)
                                                driver = privateTimer.driver
                                                isLoading = false
                                                status = Statuses.scheduling
                                                intervalTime = privateTimer.interval

                                                var previous = System.currentTimeMillis()

                                                mainCoroutine.launch {
                                                    while (intervalTime > 0) {
                                                        delay(1000L)
                                                        intervalTime -= System.currentTimeMillis() / 1000 - previous / 1000
                                                        previous = System.currentTimeMillis()
                                                    }
                                                    intervalTime = 0
                                                    privateTimer.timer.cancel()
                                                    status = Statuses.loggedIn.replace("-time-", loginTime)
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
                    }

                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) = application {
            println()
            System.setProperty(
                "webdriver.chrome.driver",
                "/opt/homebrew/Caskroom/chromedriver/105.0.5195.52/chromedriver"
            )
            val title by remember { mutableStateOf("Salarium Scheduler V2") }
            Window(
                title = title,
                onCloseRequest = ::exitApplication
            ) {
                app()
            }
        }
    }
}
