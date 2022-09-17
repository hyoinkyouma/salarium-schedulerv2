package webscrapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import tk.romanaugusto.Main
import java.time.Duration


class Webscrapper(private val email: String, private val password: String) {
    private val loggingCoroutine = CoroutineScope(Job())
    private val wait = WebDriverWait(Main.driver, Duration.ofSeconds(4))


    fun start(): ChromeDriver {

        loggingCoroutine.launch {
            Main.driver
                .manage()
                .timeouts()
                .implicitlyWait(Duration.ofSeconds(2))
            try {
                login()
                timeIn()
                Main.driver.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return Main.driver
    }

    fun login(): Boolean {
        Main.driver.get("https://app.salarium.com/users/login")
        val currentUrl = Main.driver.currentUrl
        val loginBtn = Main.driver.findElement(By.className("btn-form-custom"))
        val inputEmail = Main.driver.findElements(By.className("form-control"))
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
            currentUrl != Main.driver.currentUrl
        } catch (e: Throwable) {
            false
        }
    }

    private fun timeIn() = Main.driver.findElement(By.ById("time_btn")).click()
}