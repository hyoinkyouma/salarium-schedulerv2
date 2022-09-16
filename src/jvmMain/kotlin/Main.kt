// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tk.romanaugsto.salariumauto

import androidx.compose.runtime.*
import androidx.compose.ui.window.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.openqa.selenium.chrome.ChromeDriver
import utils.Config
import tk.romanaugsto.salariumauto.components.*

class Main {

    companion object {
        val config = Config()
        lateinit var driver: ChromeDriver
        val mainCoroutine = CoroutineScope(Job())

        data class Settings(val driverLoc: String, var cachedEmail: String, var cachedPassword: String)

        lateinit var settings: Settings

        private fun load() = config.load().let {
            settings = it
        }

        @JvmStatic
        fun main(args: Array<String>) = application {
            config.startupConfig(this)
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
                App().mainWindow()
            }
        }
    }
}
