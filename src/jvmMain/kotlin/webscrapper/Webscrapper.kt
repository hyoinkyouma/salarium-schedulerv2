package webscrapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.time.Duration


class Webscrapper(private val email: String, private val password: String) {
    private val options: ChromeOptions = ChromeOptions()
        .addArguments("--headless")
        .addArguments("--disable-gpu")
        .addArguments("--window-size=1400,800")

    val driver = ChromeDriver(options)
    private val loggingCoroutine = CoroutineScope(Job())

    fun start(): ChromeDriver {
        loggingCoroutine.launch {
            driver
                .manage()
                .timeouts()
                .implicitlyWait(Duration.ofSeconds(10))
            try {
                login()
                timeIn()
                driver.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return driver
    }

    suspend fun login() {
        driver.get("https://app.salarium.com/users/login")
        val loginBtn = driver.findElement(By.className("btn-form-custom"))
        val inputEmail = driver.findElements(By.className("form-control"))
        inputEmail[0].apply {
            this.sendKeys(email)
        }
        inputEmail[1].apply {
            this.sendKeys(password)
        }
        delay(2000L)
        loginBtn.click()
    }

    private suspend fun timeIn() = driver.findElement(By.ById("time_btn")).click()
}