package routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import app.models.ParamsMap
import app.repositories.UserRepository._
import app.traits.RoutesConfig
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

trait PersonalAccountExpandRoutes extends RoutesConfig {
  implicit def system: ActorSystem

  def userPersonalAccountActor: ActorRef

  def showPersonalData: Route =
    path("get-personal-data") {
      post {
        entity(as[ParamsMap]) { p =>
          extractCredentials {
            case Some(BasicHttpCredentials(accessId, accessToken)) =>
              val result = Future {
                checkToken(p.params.getOrElse("login", ""), accessId, accessToken) match {
                  case TokenIsValid => getPersonalData(p)
                  case UserNotAuth => UserNotAuth
                }
              }
              onComplete(result) {
                case Success(x) => x match {
                  case data: String => complete(200 -> data)
                  case UserNotAuth => complete(401 -> "user not auth")
                }
                case Failure(x) => complete(401 -> "user not auth")
              }
          }
        }
      }
    }

  def personalAccountRoutes: Route = showPersonalData
}
