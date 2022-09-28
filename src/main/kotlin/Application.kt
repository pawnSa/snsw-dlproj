import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jdk.jfr.Description
import kotlinx.serialization.Serializable
import org.litote.kmongo.*
import routes.taskRoute
import model.Task
import model.User
import org.mindrot.jbcrypt.BCrypt
import routes.accountRoute


val client = KMongo.createClient()
val database = client.getDatabase("SNSWDL")

var taskCollection = database.getCollection<Task>("tasks")
val usersCollection = database.getCollection<User>("users")

fun main(args : Array<String> ) = EngineMain.main(args)

fun Application.init() {

    install(ContentNegotiation){
        json()
    }

    install(CORS){
        allowHost("*")
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    install(Authentication){
        jwt{
            realm = "tms.com.au"
            verifier(
                JWT
                .require(Algorithm.HMAC256("secret"))
                .withAudience("http://localhost:8080")
                .withIssuer("http://localhost:8080")
                .build()
            )
            validate{
                    token -> JWTPrincipal(token.payload)
            }
            challenge{
                    defaultScheme, realm ->  call.respond(HttpStatusCode.Unauthorized,"Invalid Token")
            }
        }
    }

    routing {
        route("/account"){
            post("/register"){
                val data = call.receive<User>()
                val hashed = BCrypt.hashpw(data.password, BCrypt.gensalt())
                val user = User(data.username, password = hashed, data.firstName, data.lastName,
                    data.address, data.phone ,roles = listOf("customer"))
                usersCollection.insertOne(user)
                call.respond(HttpStatusCode.Created)
            }
        }
        accountRoute(database)

       taskRoute(taskCollection)

        authenticate {
            install(RoleBasedAuthorization) { roles = listOf("customer") }
//            accountRoute(database)
        }

    }
}

/*
@Serializable
data class Task(
    val assignee:String,
    val department:String,
    val priority:String,
    val dueDate:String,
    val description:String,
    @Serializable(with = ObjectIdAsStringSerializer::class)
    val _id: Id<Task> = newId()
)

*/