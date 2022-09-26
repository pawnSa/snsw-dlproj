package routes

import com.mongodb.client.MongoCollection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.Task
import org.bson.types.ObjectId
import org.litote.kmongo.Id
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.findOneById
import org.litote.kmongo.id.toId
import org.litote.kmongo.updateOne

fun Route.taskRoute(taskCollection : MongoCollection<Task>){

    println(taskCollection.toString())

    route("/tasks"){
        get{
            val data = taskCollection.find().toList()
            call.respond(data)
        }
        get("/{id}"){
            val idParam = call.parameters["id"].toString()
            val id : Id<Task> = ObjectId(idParam).toId()
            val task : Task? = taskCollection.findOneById(id)

            if(task != null){
                call.respond(task)
            }
            else{
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post{

            val task = call.receive<Task>();
            taskCollection.insertOne(task)
            call.respond(HttpStatusCode.Created,task)
        }

        put("/{id}"){

            val id = call.parameters["id"].toString()
            val task = call.receive<Task>();

            taskCollection.updateOne(task)
            call.respond(HttpStatusCode.OK,task)

        }

        delete("/{id}"){
            val idParam = call.parameters["id"].toString()

            //val filter = "{_id:ObjectId('$idParam')}"
            //val result = vehicleCollection.deleteOne(filter)


            val id : Id<Task> = ObjectId(idParam).toId()

            val result = taskCollection.deleteOneById(id)


            call.respond(HttpStatusCode.OK)
        }
    }

}