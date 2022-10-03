package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.MongoDatabase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.LoginRequest
import model.User
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId
import org.mindrot.jbcrypt.BCrypt
import usersCollection
import java.util.*

fun Route.noneAuthAccountRoute (database: MongoDatabase){

//    val usersCollection = database.getCollection<User>("users")

    route("/account"){

        /////////////
//        put("/{id}"){
//
//            val id = call.parameters["id"].toString()
//            val user = call.receive<User>();
//
//            usersCollection.updateOne(user)
//            call.respond(HttpStatusCode.OK,user)
//
//        }

        get("/search/{username}"){

            var username = call.parameters["username"].toString()
            var filter = "{username:'$username'}"
            var user = usersCollection.findOne(filter);
            if(user != null){
                return@get call.respond(user)
            }
            else {
                return@get call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/register"){
            val data = call.receive<User>()
            val hashed = BCrypt.hashpw(data.password, BCrypt.gensalt())
            val user = User(data.username, password = hashed, data.firstName, data.lastName,
                data.address, data.phone , data.licenseNo, data.licenseType ,
                data.licenseIssueDate,data.licenseExpiryDate,
                roles = listOf("customer"))
            usersCollection.insertOne(user)
            call.respond(HttpStatusCode.Created)
        }

        post("/login"){
            val data = call.receive<LoginRequest>()

            val filter = "{username:/^${data.username}$/i}"
            val user = usersCollection.findOne(filter)

            if(user == null){
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            val valid = BCrypt.checkpw(data.password,user.password)
            if(!valid){
                return@post call.respond(HttpStatusCode.BadRequest)
            }
            val expiry = Date(System.currentTimeMillis() + 86400000)
            val token = JWT.create()
                .withAudience("http://localhost:8080")
                .withIssuer("http://localhost:8080")
                .withClaim("username",user?.username)
                .withClaim("password",user?.password)
                .withClaim("firstName", user?.firstName)
                .withClaim("lastName", user?.lastName)
                .withClaim("address",user?.address)
                .withClaim("phone", user?.phone)
                .withClaim("licenseNo", user?.licenseNo)
                .withClaim("licenseType", user?.licenseType)
                .withClaim("licenseIssueDate", user?.licenseIssueDate)
                .withClaim("licenseExpiryDate", user?.licenseExpiryDate)
                .withClaim("roles",user?.roles)
                .withExpiresAt(expiry)
                .sign(Algorithm.HMAC256("secret"))

            return@post call.respond(token)
        }
    }
}