package nl.geoipapp.configuration.shell

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

class SSHAuthProvider(val userName: String, val password: String) : AuthProvider {

    override fun authenticate(input: JsonObject?, handler: Handler<AsyncResult<User>>?) {
        val inputUserName = input?.getString("username")
        val inputPassword = input?.getString("password")

        val isValid = inputUserName == userName && inputPassword == password
        if (isValid) {
            handler?.handle(Future.succeededFuture(SSHUser(true)))
        } else {
            handler?.handle(Future.failedFuture("Invalid username or password"))
        }
    }

}