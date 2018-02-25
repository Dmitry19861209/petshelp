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
import app.actors.UserRegistryActor.{CreateUser, GetUser}
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
            val loginUser = LoginUser(login, password)
            val userGet = userRegistryActor ? GetUser(loginUser)
            onSuccess(userGet) {
              case UserIsAuth => complete(200 -> s""" { "token" : ${UserRepository.tokenGenerate} } """)
              case UserNotAuth => complete(409 -> "user not auth")
              case _ => complete(401 -> "")
            }
            case _ => complete(HttpResponse(status = 401))
        }
      }
    }

  def register: Route =
    path("register") {
      post {
        entity(as[NewUser]) { user =>
          extractCredentials {
            case Some(BasicHttpCredentials(login, password)) =>
              val nUser = user.copy(login = login, password = password.bcrypt)
              val userCreated = userRegistryActor ? CreateUser(nUser)

              onSuccess(userCreated) {
                case UserCreated => complete(200 -> """ { "create" : "user created" } """)
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
