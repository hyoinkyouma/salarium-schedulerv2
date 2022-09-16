package utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.axay.simplekotlinmail.delivery.mailerBuilder
import net.axay.simplekotlinmail.email.emailBuilder
import org.joda.time.DateTime
import org.json.JSONObject


class Emailer(private val email: String) {
    private val emailScope = CoroutineScope(Job())
    fun send() = getAuth().let {
        val emailBody = emailBuilder {
            from("Salarium Auto Login", "SalariumAutoLogin@roman.tk")
            to(email)
            withSubject("Logged In to Salarium at ${DateTime.now().toString("hh:mm:ss dd/MM/yyyy")}")
            withPlainText("You were logged in to Salarium Payroll System at ${DateTime.now()}")
        }
        val mailer = mailerBuilder(
            host = "smtp.gmail.com",
            port = 587,
            username = it.getString("username") ?: "",
            password = it.getString("password") ?: ""
        )
        emailScope.launch {
            mailer.sendMail(emailBody)
            mailer.shutdownConnectionPool()
        }

    }

    private fun getAuth() =
        JSONObject(mapOf("username" to "salariumautologin@gmail.com", "password" to "uermnojknzxqpwid"))


}