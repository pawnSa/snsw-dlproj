package routes

import RoleBasedAuthorization
import com.mongodb.client.MongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.litote.kmongo.findOne
import usersCollection

fun Route.adminRoute (database: MongoDatabase) {

    route("/admin") {

        install(RoleBasedAuthorization) { roles = listOf("admin","customer") }

        get("/me") {

            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.claims?.get("username").toString().replace("\"", "")

            var filter = "{username:'$username'}"
            var user = usersCollection.findOne(filter);
            if (user != null) {
                return@get call.respond(user)
            } else {
                return@get call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/{Id}") {
            val id = call.parameters["id"].toString()
//            val principal = call.principal<JWTPrincipal>()
//            val userId = principal?.payload?.getClaim("id").toString().replace("\"", "")
            val user = usersCollection.findOne("{_id:ObjectId('$id')}")
            if (user != null) {
                return@get call.respond(user)
            }
            return@get call.respond(HttpStatusCode.NotFound)
        }

        get {
            val data = usersCollection.find().toList().filter{"admin" !in it.roles}
            call.respond(data)
        }
    }
}
