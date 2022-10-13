// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tk.romanaugusto

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import components.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import utils.Config
import java.util.logging.Level

class Main {
    companion object {
        //load config class
        val config = Config()

        //chrome driver initialize
        private val options: ChromeOptions = ChromeOptions()
            .setHeadless(false)

        lateinit var driver: ChromeDriver

        //main coroutine scope
        val mainCoroutine = CoroutineScope(Job())

        //data class for configs
        data class Settings(val driverLoc: String, var cachedEmail: String, var cachedPassword: String)

        lateinit var settings: Settings

        private fun load() = config.load().let {
            settings = it
        }

        @JvmStatic
        fun main(args: Array<String>) = application {
            //creates configs if not found
            config.startupConfig(this)

            //loads configs
            this@Companion.load()

            //set chrome driver to where it was copied earlier
            System.setProperty(
                "webdriver.chrome.driver",
                settings.driverLoc
            )
            System.setProperty("webdriver.chrome.silentOutput", "true")
            java.util.logging.Logger
                .getLogger("org.openqa.selenium")
                .level = Level.OFF

            driver = ChromeDriver(options)


            //creates main window
            val title by remember { mutableStateOf("Salarium Scheduler V2") }
            Window(
                title = title,
                onCloseRequest = ::exitApplication,
                resizable = false,
                icon = painterResource("drawable/icon.png")
            ) {
                App().mainWindow()
            }
        }
    }
}
