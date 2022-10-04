package model

import ObjectIdAsStringSerializer
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class DrivingHours (
    val date: String,
    val startTime: String,
    val endTime: String,
    val travelTime: Double,
    @Serializable(with = ObjectIdAsStringSerializer::class) val _id: Id<DrivingHours> = newId()
        )