package utils

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.openqa.selenium.chrome.ChromeDriver
import webscrapper.Webscrapper
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs

class Scheduler(email: String, password: String) {
    private val webscrapper = Webscrapper(email, password)

    data class PrivateTimer(val interval: Long, val timer: TimerTask, val driver: ChromeDriver)

    fun start(loginTime: String): PrivateTimer {
        val currentTime = DateTime.now().toInstant().millis
        var parsedLogin =
            (DateTimeFormat.forPattern("HH:mm:ss").parseLocalTime(loginTime)).toDateTimeToday().toInstant().millis

        if (currentTime > parsedLogin) {
            parsedLogin += 86_400_000L
        }

        val interval = abs(parsedLogin - currentTime) / 1_000L

        val timer: TimerTask = Timer("Scheduled Login")
            .schedule(delay = interval * 1_000L) {
                webscrapper.start()
            }

        return PrivateTimer(interval = interval, timer = timer, driver = webscrapper.driver)

    }
}