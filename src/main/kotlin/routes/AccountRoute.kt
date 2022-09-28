package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.MongoDatabase
import database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.Task
import model.User
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId
import org.mindrot.jbcrypt.*
import java.util.*

fun Route.accountRoute (database:MongoDatabase){

    val usersCollection = database.getCollection<User>("users")

    route("/account"){

        get("/{id}"){
            val idParam = call.parameters["id"].toString()
            val id : Id<User> = ObjectId(idParam).toId()
            val user : User? = usersCollection.findOneById(id)

            if(user != null){
                call.respond(user)
            }
            else{
                call.respond(HttpStatusCode.NotFound)
            }
        }

        /////////////////
//        get("/username"){
//            val usernameParam = call.parameters["username"].toString()
//            val data = call.receive<User>()
//
//            val filter = "{username:/^${data.username}$/i}"
//            val user = usersCollection.findOne(filter)
//            if(user != null){
//                call.respond(user)
//            }
//            else{
//                call.respond(HttpStatusCode.NotFound)
//            }
//        }
//        }



        /////////

        put("/{id}"){

            val id = call.parameters["id"].toString()
            val user = call.receive<User>();

            usersCollection.updateOne(user)
            call.respond(HttpStatusCode.OK,user)

        }


        post("/register"){
            val data = call.receive<User>()
            val hashed = BCrypt.hashpw(data.password,BCrypt.gensalt())
            val user = User(data.username, password = hashed, data.firstName, data.lastName,
                data.address, data.phone ,roles = listOf("customer"))
            usersCollection.insertOne(user)
            call.respond(HttpStatusCode.Created)
        }

        post("/login"){
            val data = call.receive<User>()

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
                .withClaim("firstName", user?.firstName)
                .withClaim("lastNane", user?.lastName)
                .withClaim("address",user?.address)
                .withClaim("phone", user?.phone)
                .withClaim("roles",user?.roles)
                .withExpiresAt(expiry)
                .sign(Algorithm.HMAC256("secret"))

            return@post call.respond(token)
        }
    }
}