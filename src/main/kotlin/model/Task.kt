package model

import ObjectIdAsStringSerializer
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

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