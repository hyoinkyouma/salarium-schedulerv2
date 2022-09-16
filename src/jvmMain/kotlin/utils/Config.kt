package utils

import org.json.JSONObject
import tk.romanaugsto.salariumauto.Main
import java.io.File

class Config {
    private val configLocation = System.getProperty("user.home") +
            File.separator +
            "Documents" +
            File.separator +
            "SalariumAutoLogin" +
            File.separator +
            "settings.json"

    fun load(): Main.Companion.Settings {
        val configJSON = JSONObject(File(configLocation).readText(Charsets.UTF_8).replace("\\", "\\\\"))
        return Main.Companion.Settings(
            cachedEmail = configJSON.optString("cachedEmail"),
            cachedPassword = configJSON.optString("cachedPassword"),
            driverLoc = configJSON.optString("driverLoc")
        )
    }

    fun save(settings: Main.Companion.Settings) = JSONObject().let {
        it.put("cachedEmail", settings.cachedEmail)
        it.put("cachedPassword", settings.cachedPassword)
        it.put("driverLoc", settings.driverLoc)
        File(configLocation).writeText(it.toString())
    }
}