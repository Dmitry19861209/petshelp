package app.services.JSON

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import app.models.{LoginUser, NewUser, User}
import spray.json._

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val loginUserFormat = jsonFormat2(LoginUser)
  implicit val newIserFormat = jsonFormat3(NewUser)
  implicit val userFormat = jsonFormat6(User)

  def serrFormat(source: String): LoginUser = {
    source.parseJson.convertTo[LoginUser]
  }
}
