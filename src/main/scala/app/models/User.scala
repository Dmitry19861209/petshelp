package app.models

import app.traits.Models
import resources.MongoDatabase
import com.mongodb.casbah.commons.MongoDBObject
import app.services.JSON.JsonSupport

case class LoginUser(login: String, password: String)
case class NewUser(login: String, password: String, role: String)
case class User(login: String, password: String, role: String, sname: String = "",
                phone: Map[String, String] = Map.empty, addresses: Array[Map[String, String]] = Array.empty)

object Users extends Models with JsonSupport {
  val collection = MongoDatabase.getCollection("users")

  def getUserByField(fieldName: String, field: String) =
    collection.findOne(MongoDBObject({fieldName} -> field))

  def createUser(userInfo: Vector[(String, Any)]) = {
    collection.insert(MongoDBObject(userInfo: _*))
  }
}
