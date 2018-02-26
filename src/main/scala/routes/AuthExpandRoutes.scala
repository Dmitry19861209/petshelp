package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{get, post, delete}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.pattern.ask
import app.actors.UserRegistryActor.{CreateUser, GetTokenByUser}
import app.models._
import app.repositories._
import app.traits.RoutesConfig
import com.github.t3hnar.bcrypt._

trait AuthExpandRoutes extends RoutesConfig {

  implicit def system: ActorSystem

  def userRegistryActor: ActorRef

  def login: Route =
    path("login") {
      get {//TODO Временный роут. Убрать
        complete(200 -> "Server is OK.")
      } ~
      post {
        extractCredentials {
          case Some(BasicHttpCredentials(login, password)) =>
            val tokenG = UserRepository.tokenGenerate
            val loginUser = LoginUser(login, password, tokenG)
            val getToken = userRegistryActor ? GetTokenByUser(loginUser)
            onSuccess(getToken) {
              case UserIsAuth => complete(200 -> s""" { "token" : $tokenG } """)
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
              val tokenG = UserRepository.tokenGenerate
              val nUser = user.copy(login = login, password = password.bcrypt, token = tokenG)
              val userCreated = userRegistryActor ? CreateUser(nUser)
              onSuccess(userCreated) {
                case UserCreated => complete(200 -> s""" { "token" : "$tokenG" } """)
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
