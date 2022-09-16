package tk.romanaugsto.salariumauto.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import net.axay.simplekotlinmail.delivery.mailerBuilder
import net.axay.simplekotlinmail.email.emailBuilder
import org.bson.Document
import org.joda.time.DateTime
import tk.romanaugsto.salariumauto.Main


class Emailer(private val email: String) {
    private val emailScope = CoroutineScope(Job())
    private val mongoDb = Main.mongoDb
    suspend fun send() = getAuth().let {
        val emailBody = emailBuilder {
            from("SalariumAutoLogin@roman.tk")
            to(email)
            withSubject("Logged In to Salarium at ${DateTime.now().toString("hh:mm:ss dd/MM/yyyy")}")
            withPlainText("You were logged in to Salarium Payroll System at ${DateTime.now()}")

        }
        val mailer = mailerBuilder(
            host = "smtp.gmail.com",
            port = 587,
            username = it?.getString("username") ?: "",
            password = it?.getString("password") ?: ""
        )
        emailScope.launch {
            mailer.sendMail(emailBody)
            mailer.shutdownConnectionPool()
        }
    }

    private suspend fun getAuth() = mongoDb.getDatabase("personal")
        .getCollection("auth")
        .find(Document(mapOf("item" to "email"))).awaitFirst()


}