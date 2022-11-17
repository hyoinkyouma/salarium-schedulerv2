package utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Dialog
import com.google.common.io.Resources
import org.json.JSONObject
import components.Dialogue
import tk.romanaugusto.Main
import java.io.File
import java.nio.charset.StandardCharsets

class Config {
    private val configLocation = System.getProperty("user.home") +
            File.separator +
            "Documents" +
            File.separator +
            "SalariumAutoLogin" +
            File.separator +
            "settings.json"

    fun load() = JSONObject(
        File(configLocation).readText(Charsets.UTF_8)
    ).let {
        Main.Companion.Settings(
            cachedEmail = it.optString("cachedEmail"),
            cachedPassword = it.optString("cachedPassword"),
            driverLoc = it.optString("driverLoc")

        )
    }


    @Composable
    fun startupConfig(applicationScope: ApplicationScope) {
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
                    .replace("-binaryLoc-", binary.replace("\\", "\\\\"))
            )
        }
        if (!File(binaryfolder).exists()) {
            File(binaryfolder).mkdir()
        }

        if (!File(binary).exists()) {
            val binaryStream = File(binary)
            //Apple
            if (System.getProperty("os.name").contains("Mac", true)) {
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
                    onCloseRequest = { applicationScope.exitApplication() },
                    title = "Error"
                ) {
                    this.window.isAlwaysOnTop = true
                    Dialogue().error(applicationScope)
                }

            }
        }
    }

    fun save(settings: Main.Companion.Settings) = JSONObject().let {
        it.put("cachedEmail", settings.cachedEmail)
        it.put("cachedPassword", settings.cachedPassword)
        it.put("driverLoc", settings.driverLoc)
        File(configLocation).writeText(it.toString())
    }
}