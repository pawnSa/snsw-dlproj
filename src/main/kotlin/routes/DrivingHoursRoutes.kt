package routes


import com.mongodb.client.MongoCollection
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import model.DrivingHours
import org.litote.kmongo.deleteOne
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.updateOne

fun Route.drivingHoursRoute(drivingHoursCollection : MongoCollection<DrivingHours>) {

    route("/drivinghours") {
        get {
            val data = drivingHoursCollection.find().toList()
            call.respond(data)
        }
        get("/{id}") {
            val id = call.parameters["id"].toString()
            val filter = "{_id:ObjectId('$id')}"
            val entity = drivingHoursCollection.findOne(filter)
            if (entity != null) {
                call.respond(entity)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
//    get("/byClientId/{clientId}"){
//        val clientId = call.parameters["clientId"].toString()
//        val filter = "{clientId:ObjectId('$clientId')}"
//        val data = patients.find(filter).toList()
//        call.respond(data)
//    }

        post("/getByIds") {
            val ids = call.receive<List<String>>()
            val wrappedIds = ids.map { id -> "ObjectId('$id')" }
            val values = wrappedIds.joinToString(",")
            val filter = "{_id: {\$in: [$values]}}"
            val entities = drivingHoursCollection.find(filter).toList()
            call.respond(entities)
        }
        post {
            val entity =
                call.receive<DrivingHours>() // if the json in the request can't be turned into a patient, there will be an error
            drivingHoursCollection.insertOne(entity)
            call.respond(HttpStatusCode.Created, entity)
        }
        put {
            val entity = call.receive<DrivingHours>()
            val result = drivingHoursCollection.updateOne(entity)
            if (result.modifiedCount.toInt() == 1) {
                call.respond(HttpStatusCode.OK, entity)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        delete("/{id}") {
            val id = call.parameters["id"].toString()
            val filter = "{_id:ObjectId('$id')}"
            val result = drivingHoursCollection.deleteOne(filter)
            if (result.deletedCount.toInt() == 1) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

