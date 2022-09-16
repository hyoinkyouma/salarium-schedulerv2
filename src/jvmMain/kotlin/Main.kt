// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tk.romanaugusto

import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import components.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.openqa.selenium.chrome.ChromeDriver
import utils.Config

class Main {
    companion object {
        //load config class
        val config = Config()

        //chrome driver initialize in webscrapper.Webscrapper
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

            //creates main window
            val title by remember { mutableStateOf("Salarium Scheduler V2") }
            Window(
                title = title,
                onCloseRequest = ::exitApplication,
                resizable = false
            ) {
                App().mainWindow()
            }
        }
    }
}
