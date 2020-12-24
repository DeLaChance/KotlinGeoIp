package nl.geoipapp.configuration.shell

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.auth.User

class SSHUser(private val isAuthorized: Boolean) : User {

    private lateinit var authProvider: AuthProvider

    override fun clearCache(): User =  this

    override fun setAuthProvider(authProvider: AuthProvider?) {
        if (authProvider != null) {
            this.authProvider = authProvider
        }
    }

    override fun isAuthorized(p0: String?, handler: Handler<AsyncResult<Boolean>>?): User {
        handler?.handle(Future.succeededFuture(isAuthorized))
        return this
    }

    override fun principal(): JsonObject = JsonObject()

}