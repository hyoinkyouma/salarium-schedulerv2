// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import utils.Scheduler


class Main {
    companion object {
        private val mainCoroutine = CoroutineScope(Job())
        private fun getIntervalTime(interval: Long): String {
            val hours = interval / 3600
            val minutes = (interval % 3600L).floorDiv(60L)
            val seconds = interval % 60
            return "${hours}:${minutes}:${seconds}"
        }

        @Composable
        fun app() {
            val currentTime = DateTime.now().plusHours(1).toString("hh:mm:ss")
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var loginTime by remember { mutableStateOf(currentTime) }
            var intervalTime by remember { mutableStateOf<Long>(0L) }
            var isLoading by remember { mutableStateOf(false) }
            var isError by remember { mutableStateOf(false) }

            MaterialTheme {
                Row(
                    Modifier.fillMaxHeight().background(
                        color = Color(0x0F172A).copy(alpha = 1f)
                    ),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(70.dp)
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
                                                isError = isError,
                                                label = { Text("Login Time (H:M:S)") }
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
                                        onClick = { intervalTime = 0 },
                                        enabled = intervalTime != 0L
                                    ) {
                                        Text("Cancel")
                                    }
                                    Button(
                                        enabled = !isLoading && intervalTime == 0L,
                                        onClick = {
                                            isLoading = true
                                            mainCoroutine.launch {
                                                val privateTimer = Scheduler(email, password).start(loginTime)
                                                isLoading = false
                                                if (privateTimer != null) {
                                                    intervalTime = privateTimer.interval
                                                }
                                                var previous = System.currentTimeMillis()


                                                mainCoroutine.launch {
                                                    while (intervalTime > 0) {
                                                        delay(1000L)
                                                        intervalTime -= System.currentTimeMillis() / 1000 - previous / 1000
                                                        previous = System.currentTimeMillis()
                                                    }
                                                    intervalTime = 0
                                                    if (intervalTime == 0L) {
                                                        privateTimer?.timer?.cancel()
                                                    }
                                                }
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
