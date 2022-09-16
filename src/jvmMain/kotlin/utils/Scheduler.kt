package utils

import com.google.common.primitives.UnsignedInts.toLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.openqa.selenium.chrome.ChromeDriver
import webscrapper.Webscrapper
import java.sql.Time
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs

class Scheduler(private val email: String, private val password: String) {
    private val coroutineScope = CoroutineScope(Job())
    private val webscrapper = Webscrapper(email, password)

    data class privateTimer(val interval: Long, val timer: TimerTask, val driver: ChromeDriver)

    fun start(loginTime: String): privateTimer {
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

        return privateTimer(interval = interval, timer = timer, driver = webscrapper.driver)

    }
}