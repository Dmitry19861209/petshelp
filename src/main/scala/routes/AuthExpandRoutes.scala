package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.Directives.{path, post, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import app.actors.UserRegistryActor.CreateNewUser
import app.models._
import app.repositories.UserRepository._
import app.traits.RoutesConfig
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

trait AuthExpandRoutes extends RoutesConfig {

  implicit def system: ActorSystem

  def userRegistryActor: ActorRef

  def login: Route =
    path("login") {
      get {
        //TODO Временный роут. Убрать
        complete(200 -> "Server is OK.")
      } ~
      post {
        entity(as[ParamsMap]) { params =>
          extractCredentials {
            case Some(BasicHttpCredentials(login, password)) =>
              val getToken = Future { checkPass(login, password, params) }
              onComplete(getToken) {
                case Success(x) => x match {
                  case UserIsAuth(t) =>
                    complete(200 -> s""" { "accessToken" : "${t("accessToken")}", "accessId" : "${t("accessId")}" } """)
                  case UserNotAuth => complete(401 -> "user not auth")
                  case _ => complete(401 -> "")
                }
                case Failure(x) => complete(401 -> "")
              }
            case _ => complete(HttpResponse(status = 401))
          }
        }
      }
    }

  def register: Route =
    path("register") {
      post {
        entity(as[User]) { user =>
          extractCredentials {
            case Some(BasicHttpCredentials(login, password)) =>
              val userCreated = userRegistryActor ? CreateNewUser(login, password, user)
              onSuccess(userCreated) {
                case UserCreated(token) =>
                  val t = token
                  val accessToken = t("accessToken")
                  val accessId = t("accessId")
                  complete(200 -> s""" { "accessToken" : "$accessToken", "accessId" : "$accessId" } """)
                case UserAlreadyExist => complete(409 -> "user already exists")
                case _ => complete(401 -> "")
              }
            case _ => complete(401 -> "")
          }
        }
      }
    }

  def authRoutes: Route = login ~ register
}
