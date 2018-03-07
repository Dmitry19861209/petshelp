package app.models

import app.traits.Models
import resources.MongoDatabase
import com.mongodb.casbah.commons.{MongoDBObject, TypeImports}
import com.mongodb.casbah.Imports._

case class LoginUser(login: String, password: String, token: Array[Map[String, String]])
case class User(login: String,  password: String, token: Array[Map[String, String]], role: String, sname: String,
                phone: Map[String, String], addresses: Array[Map[String, String]]) {
//  def this(login: String, password: String, token: String, role: String) = this(login, password, token, role, "", Map.empty, Array.empty)
}
case class ParamsMap(params: Map[String, String])

object Users extends Models {
  val collection = MongoDatabase.getCollection("users")

  def getUserByField(fieldName: String, field: String): Option[TypeImports.DBObject] =
    collection.findOne(MongoDBObject({fieldName} -> field))

  def createUser(userInfo: Vector[(String, Any)]) = {
    collection.insert(MongoDBObject(userInfo: _*))
  }

  def tokenUpdate(fieldName: String, login: String, token: Array[Map[String, String]]) = {
    val builder = collection.initializeOrderedBulkOperation
    builder.find(MongoDBObject("login" -> login)).updateOne($set("token" -> token))
    val result = builder.execute()
  }
}
