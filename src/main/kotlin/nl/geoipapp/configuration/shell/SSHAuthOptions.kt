package nl.geoipapp.configuration.shell

import io.vertx.core.Vertx
import io.vertx.ext.auth.AuthOptions
import io.vertx.ext.auth.AuthProvider
import nl.geoipapp.util.getNestedString
import java.lang.IllegalArgumentException

class SSHAuthOptions : AuthOptions {

    override fun clone(): AuthOptions = SSHAuthOptions()

    override fun createProvider(vertx: Vertx?): AuthProvider {
        if (vertx == null) {
            throw IllegalArgumentException("Vertx instance cannot be null!")
        } else {
            val globalConfig = vertx.orCreateContext.config()
            val sshUserName = globalConfig.getNestedString("ssh.user", "vertx")
            val sshUserPassword = globalConfig.getNestedString("ssh.password", "vertx")
            return SSHAuthProvider(userName = sshUserName, password = sshUserPassword)
        }
    }

}
