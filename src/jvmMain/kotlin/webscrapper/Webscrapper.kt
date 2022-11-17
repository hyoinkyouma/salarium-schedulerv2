package webscrapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import utils.Emailer
import java.time.Duration


class Webscrapper(private val email: String, private val password: String) {
    private val loggingCoroutine = CoroutineScope(Job())
    private val options: ChromeOptions = ChromeOptions()
        .setHeadless(true)
    private lateinit var driver:ChromeDriver
    fun start() {
        loggingCoroutine.launch {

            var isSuccess = false
            var retryCount = 0
            while (!isSuccess && retryCount <= 10) {
                isSuccess = try {
                    driver = ChromeDriver(options)
                    driver
                        .manage()
                        .timeouts()
                        .implicitlyWait(Duration.ofSeconds(4))
                    login(isMain = true)
                    timeIn()
                    delay(2_000L)
                    driver.get("https://app.salarium.com/users/logout")
                    driver.close()
                    true
                } catch (e: Throwable) {
                    e.printStackTrace()
                    retryCount++
                    driver.close()
                    false
                }
            }
            if (!isSuccess){
                Emailer(email).sendFailed()
            }
        }
    }


    fun login(isMain:Boolean = false): Boolean {
        if (!isMain) {
            driver = ChromeDriver(options)
        }
        val wait = WebDriverWait(driver, Duration.ofSeconds(4L))
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
            val url = driver.currentUrl
            if (!isMain){
                driver.close()
            }
            currentUrl != url
        } catch (e: Throwable) {
            driver.close()
            false
        }
    }

    private fun timeIn() = driver.findElement(By.ById("time_btn")).click()
}