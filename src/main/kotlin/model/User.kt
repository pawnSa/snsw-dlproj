package model

import ObjectIdAsStringSerializer
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class User(
    val username:String,    //Email address only
    val password:String,
    val firstName: String,
    val lastName: String,
    val address: String,
    val phone: Int,
    val licenseNo: String = "N/A",
    val licenseType:String= "N/A",
    val licenseIssueDate:String= "N/A",
    val licenseExpiryDate:String= "N/A",
    val roles : List<String> = listOf(),
    val logbookHours: MutableList<DrivingHours> = mutableListOf(),
    @Serializable(with = ObjectIdAsStringSerializer::class)
    val _id: Id<User> = newId()
)