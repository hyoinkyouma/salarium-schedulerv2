package webscrapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverLogLevel
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration


class Webscrapper(private val email: String, private val password: String) {

    private val options: ChromeOptions = ChromeOptions()
        .setLogLevel(ChromeDriverLogLevel.OFF)
        .setHeadless(true)

    val driver = ChromeDriver(options)
    private val loggingCoroutine = CoroutineScope(Job())
    private val wait = WebDriverWait(driver, Duration.ofSeconds(4))


    fun start(): ChromeDriver {

        loggingCoroutine.launch {
            driver
                .manage()
                .timeouts()
                .implicitlyWait(Duration.ofSeconds(2))
            try {
                login()
                //    timeIn()
                driver.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return driver
    }

    fun login(): Boolean {
        driver.get("https://app.salarium.com/users/login")
        val currentUrl = driver.currentUrl
        val loginBtn = driver.findElement(By.className("btn-form-custom"))
        val inputEmail = driver.findElements(By.className("form-control"))
        inputEmail[0].apply {
            this.sendKeys(email)
        }
        inputEmail[1].apply {
            this.sendKeys(password)
        }
        return try {
            wait.until(ExpectedConditions.attributeToBe(inputEmail[1], "value", password))
            loginBtn.click()
            wait.until(ExpectedConditions.urlToBe("https://app.salarium.com/employees/page/dashboard"))
            currentUrl != driver.currentUrl
        } catch (e: Throwable) {
            false
        }
    }

    private fun timeIn() = driver.findElement(By.ById("time_btn")).click()
}