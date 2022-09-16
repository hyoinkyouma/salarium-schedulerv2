// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tk.romanaugsto.salariumauto


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
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import androidx.compose.ui.zIndex
import com.google.common.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.openqa.selenium.chrome.ChromeDriver
import utils.Config

import utils.Scheduler
import webscrapper.Webscrapper
import java.io.File
import java.nio.charset.StandardCharsets


class Main {
    object Statuses {
        const val incorrectCreds = "Password or Email is incorrect"
        const val isLoadingService = "Spinning up chrome background service"
        const val scheduling = "Scheduling tasks"
        const val cancelled = "Task was cancelled"
        const val loggingIn = "Logging in at -time-"
        const val incorrectDate = "Time format must be 24 time format and must be at a later time"
        const val loggedIn = "Logged in at -time-"
    }

    companion object {
        private val config = Config()
        private lateinit var driver: ChromeDriver
        private val mainCoroutine = CoroutineScope(Job())
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

        data class Settings(val driverLoc: String, var cachedEmail: String, var cachedPassword: String)

        private lateinit var settings: Settings

        private fun load() = config.load().let {
            settings = it
        }

        @Composable
        fun error(applicationScope: ApplicationScope) {
            MaterialTheme {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        Modifier
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally

                        ) {
                            Text(
                                "Error",
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold

                            )
                            Text(
                                ">_<",
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                modifier = Modifier.padding(horizontal = 30.dp),
                                text = "There are currently no compiled chrome binaries available for your platform",
                                textAlign = TextAlign.Center,
                            )
                            Button(
                                modifier = Modifier.padding(top = 20.dp),
                                onClick = { applicationScope.exitApplication() }
                            ) {
                                Text("Ok :(")
                            }
                        }

                    }
                }
            }
        }

        @Composable
        fun app() {
            val currentTime = DateTime.now().plusMinutes(2).toString("HH:mm:ss")
            var email by remember { mutableStateOf(settings.cachedEmail) }
            var password by remember { mutableStateOf(settings.cachedPassword) }
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
                                            if (email.isEmpty() || password.isEmpty()) {
                                                status = "Credentials are empty."
                                                statusColor = Color.Red
                                                mainCoroutine.launch {
                                                    delay(2000)
                                                    status = ""
                                                    return@launch
                                                }
                                                return@Button
                                            }
                                            if (!loginTime.matches(Regex("(?:[01]\\d|2[0123]):[012345]\\d:[012345]\\d"))) {
                                                status = Statuses.incorrectDate
                                                statusColor = Color.Red
                                                mainCoroutine.launch {
                                                    delay(2000)
                                                    status = ""
                                                    return@launch
                                                }
                                                return@Button
                                            }
                                            statusColor = Color.White
                                            status = ""
                                            isLoading = true
                                            mainCoroutine.launch {
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
                                                settings.cachedEmail = email
                                                settings.cachedPassword = password
                                                config.save(settings)
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

        @JvmStatic
        fun main(args: Array<String>) = application {

            println("[APPLICATION]: Detect OS -> ${System.getProperty("os.name")}")
            println("[APPLICATION]: Detect Architecture -> ${System.getProperty("os.arch")}")

            val settingsFolderLocation =
                System.getProperty("user.home") + File.separator + "Documents" + File.separator + "SalariumAutoLogin"
            val settingsFile = settingsFolderLocation + File.separator + "settings.json"
            val binaryfolder = settingsFolderLocation + File.separator + "binaries"
            val binary = binaryfolder + File.separator + "chromewebdriver"

            if (!File(settingsFolderLocation).exists()) {
                File(settingsFolderLocation).mkdir()
            }
            if (!File(settingsFile).exists()) {
                File(settingsFile).writeText(
                    Resources.getResource("config/settings.json")
                        .readText(StandardCharsets.UTF_8)
                        .replace("-binaryLoc-", binary)
                )
            }
            if (!File(binaryfolder).exists()) {
                File(binaryfolder).mkdir()
            }

            if (!File(binary).exists()) {
                val binaryStream = File(binary)
                //Apple
                if (System.getProperty("os.name").contains("MAC", true)) {
                    println("[APPLICATION]: Install chromedriver")

                    //M1 && M2
                    if (System.getProperty("os.arch") == "aarch64") {
                        useResource("driver/chromedriver") {
                            it.copyTo(binaryStream.outputStream())
                            binaryStream.setExecutable(true)
                        }
                    }
                    //Intel
                    else {
                        useResource("driver/chromedriverintelmac") {
                            it.copyTo(binaryStream.outputStream())
                            binaryStream.setExecutable(true)
                        }
                    }
                }
                //Windows
                else if (System.getProperty("os.name").contains("Windows", ignoreCase = true)) {
                    useResource("driver/chromedriver.exe") {
                        it.copyTo(binaryStream.outputStream())
                        binaryStream.setExecutable(true)
                    }
                } else {
                    Dialog(
                        onCloseRequest = ::exitApplication,
                        title = "Error"
                    ) {
                        error(this@application)
                    }
                    return@application
                }
            }

            this@Companion.load()
            System.setProperty(
                "webdriver.chrome.driver",
                settings.driverLoc
            )
            val title by remember { mutableStateOf("Salarium Scheduler V2") }
            Window(
                title = title,
                onCloseRequest = ::exitApplication,
                resizable = false
            ) {
                app()
            }
        }
    }
}
