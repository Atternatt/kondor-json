package com.ubertob.kondor.outcome

import org.junit.jupiter.api.Test

internal class OutcomeTest {

    data class Err(override val msg: String) : OutcomeError

    data class User(val name: String, val email: String)

    fun getUser(id: Int): BaseOutcome<User> =
        if (id > 0) User("u$id", "$id@example.com").asSuccess() else Err("wrong id").asFailure()

    fun getMailText(name: String): BaseOutcome<String> =
        if (name.isEmpty()) Err("no name").asFailure() else "Hello $name".asSuccess()

    fun sendEmailUser(email: String, text: String): UnitOutcome =
        if (text.isNotEmpty() && email.isNotEmpty()) Unit.asSuccess() else Err("empty text or email").asFailure()


    @Test
    fun bindingComposition() {

        val res: UnitOutcome = getUser(123)
            .bind { u ->
                getMailText(u.name)
                    .bind { t ->
                        sendEmailUser(u.email, t)
                    }
            }
        res.expectSuccess()
    }

    @Test
    fun bindingCompositionOnFailure() {

        val res = fun(): UnitOutcome {
            val u = getUser(123).onFailure { return it.asFailure() }
            val t = getMailText(u.name).onFailure { return it.asFailure() }
            val e = sendEmailUser(u.email, t).onFailure { return it.asFailure() }
            return e.asSuccess()

        }

        res().expectSuccess()

    }

}