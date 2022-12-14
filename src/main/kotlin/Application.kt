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
import model.DrivingHours
import org.litote.kmongo.*


import model.User
import org.mindrot.jbcrypt.BCrypt
import routes.accountRoute
import routes.adminRoute
import routes.drivingHoursRoute
import routes.noneAuthAccountRoute

val client = KMongo.createClient()
val database = client.getDatabase("SNSWDL")
val usersCollection = database.getCollection<User>("users")
var drivingHoursCollection = database.getCollection<DrivingHours>("drivinghours")

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

//          accountRoute(database)
//          None Authenticated routes
      noneAuthAccountRoute(database)


        authenticate {
            accountRoute(database)
            drivingHoursRoute(drivingHoursCollection)
            adminRoute(database)


        }

    }
}
