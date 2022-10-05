package routes

import RoleBasedAuthorization
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.MongoDatabase
import database
import getEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.DrivingHours
import model.User
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId
import org.mindrot.jbcrypt.*
import usersCollection
import java.util.*

//val usersCollection = database.getCollection<User>("users")
fun Route.accountRoute (database:MongoDatabase) {
    install(RoleBasedAuthorization) { roles = listOf("customer") }

    route("/account") {

        post("/logbook-entry") {
            val entry = call.receive<DrivingHours>()
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("id").toString().replace("\"", "")
            val user = usersCollection.findOne("{_id: ObjectId('$userId')}")
            if (user != null) {
                user.logbookHours.add(entry)
                usersCollection.updateOne(user)
                return@post call.respond(HttpStatusCode.Created, entry)
            }
            return@post call.respond(HttpStatusCode.NotFound)
        }

        put("/{id}") {
            val data = call.receive<User>()
            val id = call.parameters["id"].toString()
//            val principal = call.principal<JWTPrincipal>()
//            val username = principal?.payload?.getClaim("username").toString().replace("\"", "")
            val filter = "{_id:ObjectId('$id')}"
            var user = usersCollection.findOne(filter);
            val clone = user?.copy(licenseType = data.licenseType, licenseIssueDate = data.licenseIssueDate, licenseExpiryDate = data.licenseExpiryDate,  licenseNo = user._id.toString())
            if (clone != null) {
                usersCollection.updateOne(clone)
                return@put call.respond(HttpStatusCode.OK, clone)
            }
            return@put call.respond(HttpStatusCode.NotFound)
        }

        /////// Update with /me directory. Meaning same directory as user area. See get() method below

        put("/me") {

            val data = call.receive<User>()
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.getClaim("username").toString().replace("\"", "")

            var filter = "{username:'$username'}"
            var user = usersCollection.findOne(filter);

            val clone = user?.copy(address = data.address, phone = data.phone)

            if (clone != null) {
                usersCollection.updateOne(clone)

                return@put call.respond(HttpStatusCode.OK, clone)
            }

            return@put call.respond(HttpStatusCode.NotFound)

        }


        get("/{id}") {
            val idParam = call.parameters["id"].toString()
            val id: Id<User> = ObjectId(idParam).toId()
            val user: User? = usersCollection.findOneById(id)

            if (user != null) {
                call.respond(user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

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
    }
}
