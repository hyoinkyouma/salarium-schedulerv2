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
import utils.Config
import java.util.logging.Level

class Main {
    companion object {
        //load config class
        val config = Config()

        //main coroutine scope
        val mainCoroutine = CoroutineScope(Job())

        //data class for configs
        data class Settings(val driverLoc: String, var cachedEmail: String, var cachedPassword: String)

        lateinit var settings: Settings

        @JvmStatic
        fun main(args: Array<String>) = application {
            //creates configs if not found
            config.startupConfig(this)


            //loads configs
            settings = config.load()

            //set chrome driver to where it was copied earlier
            System.setProperty(
                "webdriver.chrome.driver",
                settings.driverLoc
            )

            //creates main window
            val title by remember { mutableStateOf("Salarium Scheduler V2") }

            Window(
                title = title,
                onCloseRequest = {
                    exitApplication()
                },
                resizable = false,
                icon = painterResource("drawable/icon.png")
            ) {
                App().mainWindow()
            }
        }
    }
}
