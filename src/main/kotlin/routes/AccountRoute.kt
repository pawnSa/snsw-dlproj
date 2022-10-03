package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mongodb.client.MongoDatabase
import database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.LoginRequest
import model.Task
import model.User
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.id.toId
import org.mindrot.jbcrypt.*
import usersCollection
import java.util.*

//val usersCollection = database.getCollection<User>("users")
fun Route.accountRoute (database:MongoDatabase){

    route("/account"){

        ////////////    TRY THIS PUT  /// Update with ID
        put("/{id}"){

            val data = call.receive<User>()
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.getClaim("username").toString().replace("\"","")

            var filter = "{username:'$username'}"
            var user = usersCollection.findOne(filter);

            val clone = user?.copy(address=data.address,phone = data.phone)

            if(clone != null){
                usersCollection.updateOne(clone)
                return@put call.respond(HttpStatusCode.OK,clone)
            }

            return@put call.respond(HttpStatusCode.NotFound)

        }

        /////// Update with /me directory. Meaning same directory as user area. See get() method below

        put("/me"){

            val data = call.receive<User>()
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.getClaim("username").toString().replace("\"","")

            var filter = "{username:'$username'}"
            var user = usersCollection.findOne(filter);

            val clone = user?.copy(address=data.address,phone = data.phone)

            if(clone != null){
                usersCollection.updateOne(clone)

                return@put call.respond(HttpStatusCode.OK,clone)
            }

            return@put call.respond(HttpStatusCode.NotFound)

        }
        /////////
//        put("/{id}"){
//
//            val id = call.parameters["id"].toString()
//            val user = call.receive<User>();
//
//            usersCollection.updateOne(user)
//            call.respond(HttpStatusCode.OK,user)
//        }
        /////////

//
//        get("/loggedinuser"){
//
//        }

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

        get("/me"){

            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.claims?.get("username").toString().replace("\"","")

            var filter = "{username:'$username'}"
            var user = usersCollection.findOne(filter);
            if(user != null){
                return@get call.respond(user)
            }
            else {
                return@get call.respond(HttpStatusCode.NotFound)
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

//        post("/register"){
//            val data = call.receive<User>()
//            val hashed = BCrypt.hashpw(data.password,BCrypt.gensalt())
//            val user = User(data.username, password = hashed, data.firstName, data.lastName,
//                data.address, data.phone ,roles = listOf("customer"))
//            usersCollection.insertOne(user)
//            call.respond(HttpStatusCode.Created)
//        }

        /*
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
//                .withClaim("password",user?.password)
                .withClaim("firstName", user?.firstName)
                .withClaim("lastNane", user?.lastName)
                .withClaim("address",user?.address)
                .withClaim("phone", user?.phone)
                .withClaim("roles",user?.roles)
                .withExpiresAt(expiry)
                .sign(Algorithm.HMAC256("secret"))

            return@post call.respond(token)
        }

         */
    }
}