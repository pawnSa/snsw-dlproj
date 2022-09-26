import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import jdk.jfr.Description
import kotlinx.serialization.Serializable
import org.litote.kmongo.*
import routes.taskRoute

import model.Task


val client = KMongo.createClient()
val database = client.getDatabase("taskDb")
var taskCollection = database.getCollection<Task>("tasks")



fun main(args : Array<String> ) = EngineMain.main(args)


fun Application.init() {

    install(ContentNegotiation){
        json()
    }

    install(CORS){
        allowHost("*")
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    routing {

        taskRoute(taskCollection)

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