package app.services.JSON

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import app.models.{LoginUser, User, ParamsMap}
import spray.json._

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val paramsMapFormat = jsonFormat1(ParamsMap)
  implicit val loginUserFormat = jsonFormat3(LoginUser)
  implicit val userFormat = jsonFormat7(User)

  def serrFormat(source: String): LoginUser = {
    source.parseJson.convertTo[LoginUser]
  }
//TODO Объединить serrFormat в один метод!
  def serrFormatForUser(source: String): User = {
    source.parseJson.convertTo[User]
  }
}
