package resources

import app.traits.Database
import com.mongodb.casbah.Imports._

object MongoDatabase extends Database {
  val mongoClient =  MongoClient()
  val db = mongoClient("petshelp")

  def getCollection(name: String): MongoCollection = db(name)
}
