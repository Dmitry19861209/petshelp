package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.Directives.{path, post, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import app.actors.UserRegistryActor.{CreateNewUser, GetTokenByUser}
import app.models._
import app.repositories._
import app.traits.RoutesConfig

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
        extractCredentials {
          case Some(BasicHttpCredentials(login, password)) =>
            val getToken = userRegistryActor ? GetTokenByUser(login, password)
            onSuccess(getToken) {
              case UserIsAuth(token) =>
                val t = token(0)
                val access_token = t("access_token")
                val id = t("id")
                complete(200 -> s""" { "token" : "$access_token", "id" : $id } """)
              case UserNotAuth => complete(401 -> "user not auth")
              case _ => complete(401 -> "")
            }
          case _ => complete(HttpResponse(status = 401))
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
                  val t = token(0)
                  val access_token = t("access_token")
                  val id = t("id")
                  complete(200 -> s""" { "token" : "$access_token", "id" : $id } """)
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
