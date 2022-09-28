package model

import ObjectIdAsStringSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.Date

@Serializable
data class User(
    val username:String,    //Email address only
    val password:String,
    val firstName: String,
    val lastName: String,
//    @Contextual
//    val dob: Date,
    val address: String,
    val phone: Int,
    val roles : List<String> = listOf(),
    @Serializable(with = ObjectIdAsStringSerializer::class)
    val _id: Id<User> = newId()
)