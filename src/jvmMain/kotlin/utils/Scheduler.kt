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
import kotlin.concurrent.schedule
import java.util.Timer
import java.util.TimerTask

class Scheduler(private val email: String, private val password: String) {
    private val coroutineScope = CoroutineScope(Job())
    private val webscrapper = Webscrapper(email, password)

    data class privateTimer(val interval: Long, val timer: TimerTask, val driver: ChromeDriver)

    fun start(loginTime: String): privateTimer? {
        val currentTime = DateTime.now().toInstant().millis
        val parsedLogin =
            (DateTimeFormat.forPattern("HH:mm:ss").parseLocalTime(loginTime)).toDateTimeToday().toInstant().millis
        val interval = (parsedLogin - currentTime) / 1000L
        if (currentTime >= parsedLogin) {
            return null
        }
        lateinit var timer: TimerTask
        coroutineScope.run {
            timer = Timer("Scheduled Login")
                .schedule(delay = interval * 1000) {
                    webscrapper.start()
                }

        }
        return privateTimer(interval = interval, timer = timer, driver = webscrapper.driver)

    }
}